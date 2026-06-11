#include <cstdint>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <string>
#include <winsock2.h>
#include <ws2tcpip.h>
#include "sqlite3.h"

#ifdef _MSC_VER
#pragma comment(lib, "ws2_32.lib")
#endif

sqlite3* db = nullptr;

constexpr uint32_t K[64] = {
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
    0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
    0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
    0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
    0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
    0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
    0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
    0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
    0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
};

uint32_t rotr(uint32_t x, int n) { return (x >> n) | (x << (32 - n)); }
uint32_t ch(uint32_t x, uint32_t y, uint32_t z) { return (x & y) ^ (~x & z); }
uint32_t maj(uint32_t x, uint32_t y, uint32_t z) { return (x & y) ^ (x & z) ^ (y & z); }
uint32_t ep0(uint32_t x) { return rotr(x, 2) ^ rotr(x, 13) ^ rotr(x, 22); }
uint32_t ep1(uint32_t x) { return rotr(x, 6) ^ rotr(x, 11) ^ rotr(x, 25); }
uint32_t sig0(uint32_t x) { return rotr(x, 7) ^ rotr(x, 18) ^ (x >> 3); }
uint32_t sig1(uint32_t x) { return rotr(x, 17) ^ rotr(x, 19) ^ (x >> 10); }

std::string sha256(const std::string& input) {
    uint32_t h[8] = {
        0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
        0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    };

    std::string msg = input;
    uint64_t bits = msg.size() * 8;
    msg += char(0x80);
    while (msg.size() % 64 != 56) msg += char(0);
    for (int i = 7; i >= 0; --i) msg += char((bits >> (i * 8)) & 0xff);

    for (size_t i = 0; i < msg.size(); i += 64) {
        uint32_t w[64] = {};
        for (int j = 0; j < 16; ++j) {
            size_t p = i + j * 4;
            w[j] = (uint8_t(msg[p]) << 24) | (uint8_t(msg[p + 1]) << 16) |
                   (uint8_t(msg[p + 2]) << 8) | uint8_t(msg[p + 3]);
        }
        for (int j = 16; j < 64; ++j)
            w[j] = sig1(w[j - 2]) + w[j - 7] + sig0(w[j - 15]) + w[j - 16];

        uint32_t a = h[0], b = h[1], c = h[2], d = h[3];
        uint32_t e = h[4], f = h[5], g = h[6], hh = h[7];

        for (int j = 0; j < 64; ++j) {
            uint32_t t1 = hh + ep1(e) + ch(e, f, g) + K[j] + w[j];
            uint32_t t2 = ep0(a) + maj(a, b, c);
            hh = g; g = f; f = e; e = d + t1;
            d = c; c = b; b = a; a = t1 + t2;
        }

        h[0] += a; h[1] += b; h[2] += c; h[3] += d;
        h[4] += e; h[5] += f; h[6] += g; h[7] += hh;
    }

    char result[65] = {};
    for (int i = 0; i < 8; ++i) std::snprintf(result + i * 8, 9, "%08x", h[i]);
    return result;
}

sqlite3_stmt* prepare(const char* sql) {
    sqlite3_stmt* stmt = nullptr;
    return sqlite3_prepare_v2(db, sql, -1, &stmt, nullptr) == SQLITE_OK ? stmt : nullptr;
}

void bindText(sqlite3_stmt* stmt, int index, const std::string& value) {
    sqlite3_bind_text(stmt, index, value.c_str(), -1, SQLITE_TRANSIENT);
}

std::string columnText(sqlite3_stmt* stmt, int column) {
    const unsigned char* text = sqlite3_column_text(stmt, column);
    return text ? reinterpret_cast<const char*>(text) : "";
}

double currentBalance() {
    sqlite3_stmt* stmt = prepare("SELECT balance FROM users WHERE id = 1");
    double balance = 0;
    if (stmt && sqlite3_step(stmt) == SQLITE_ROW) balance = sqlite3_column_double(stmt, 0);
    sqlite3_finalize(stmt);
    return balance;
}

bool runBalanceUpdate(double balance) {
    sqlite3_stmt* stmt = prepare("UPDATE users SET balance = ? WHERE id = 1");
    if (!stmt) return false;
    sqlite3_bind_double(stmt, 1, balance);
    bool ok = sqlite3_step(stmt) == SQLITE_DONE;
    sqlite3_finalize(stmt);
    return ok;
}

bool addTransaction(const char* type, const std::string& name, double amount) {
    sqlite3_stmt* stmt = prepare(
        "INSERT INTO transactions (transaction_type, party_name, amount) VALUES (?, ?, ?)"
    );
    if (!stmt) return false;
    sqlite3_bind_text(stmt, 1, type, -1, SQLITE_STATIC);
    bindText(stmt, 2, name);
    sqlite3_bind_double(stmt, 3, amount);
    bool ok = sqlite3_step(stmt) == SQLITE_DONE;
    sqlite3_finalize(stmt);
    return ok;
}

std::string handleLogin(const std::string& email, const std::string& password) {
    sqlite3_stmt* stmt = prepare("SELECT 1 FROM users WHERE email = ? AND password_hash = ? LIMIT 1");
    if (!stmt) return "FAIL";
    bindText(stmt, 1, email);
    bindText(stmt, 2, sha256(password));
    bool ok = sqlite3_step(stmt) == SQLITE_ROW;
    sqlite3_finalize(stmt);
    return ok ? "SUCCESS" : "FAIL";
}

std::string handleGetBalance() {
    return std::to_string(currentBalance());
}

std::string handleGetBeneficiaries() {
    sqlite3_stmt* stmt = prepare("SELECT ben_name FROM beneficiaries");
    if (!stmt) return "END\n";
    std::string result;
    while (sqlite3_step(stmt) == SQLITE_ROW) result += columnText(stmt, 0) + "\n";
    sqlite3_finalize(stmt);
    return result + "END\n";
}

std::string handleAddBeneficiary(const std::string& name, const std::string& account, const std::string& ifsc) {
    sqlite3_stmt* stmt = prepare(
        "INSERT INTO beneficiaries (ben_name, account_no, ifsc_code) VALUES (?, ?, ?)"
    );
    if (!stmt) return "FAIL";
    bindText(stmt, 1, name);
    bindText(stmt, 2, account);
    bindText(stmt, 3, ifsc);
    bool ok = sqlite3_step(stmt) == SQLITE_DONE;
    sqlite3_finalize(stmt);
    return ok ? "SUCCESS" : "FAIL";
}

std::string transfer(const std::string& name, double amount, bool receive) {
    if (amount <= 0) return "FAIL";
    double balance = currentBalance();
    if (!receive && balance < amount) return "FAIL";
    double newBalance = receive ? balance + amount : balance - amount;
    const char* type = receive ? "RECEIVED" : "SENT";
    return (runBalanceUpdate(newBalance) && addTransaction(type, name, amount)) ? "SUCCESS" : "FAIL";
}

std::string handleGetTransactions() {
    sqlite3_stmt* stmt = prepare(
        "SELECT id, transaction_type, party_name, amount, created_at FROM transactions"
    );
    if (!stmt) return "END\n";
    std::string result;
    while (sqlite3_step(stmt) == SQLITE_ROW) {
        std::string partyName = columnText(stmt, 2);
        for (char& c : partyName) if (c == '_') c = ' ';
        result += std::to_string(sqlite3_column_int(stmt, 0)) + ",";
        result += columnText(stmt, 1) + ",";
        result += partyName + ",";
        result += std::to_string(sqlite3_column_double(stmt, 3)) + ",";
        result += columnText(stmt, 4) + "\n";
    }
    sqlite3_finalize(stmt);
    return result + "END\n";
}

std::string handleCommand(const std::string& command) {
    std::istringstream in(command);
    std::string cmd;
    in >> cmd;

    if (cmd == "LOGIN") {
        std::string email, password;
        return (in >> email >> password) ? handleLogin(email, password) : "FAIL";
    }
    if (cmd == "GET_BALANCE")       return handleGetBalance();
    if (cmd == "GET_BENEFICIARIES") return handleGetBeneficiaries();
    if (cmd == "GET_TRANSACTIONS")  return handleGetTransactions();
    if (cmd == "ADD_BENEFICIARY") {
        std::string name, account, ifsc;
        return (in >> name >> account >> ifsc) ? handleAddBeneficiary(name, account, ifsc) : "FAIL";
    }
    if (cmd == "SEND" || cmd == "RECEIVE") {
        std::string name;
        double amount = 0;
        return (in >> name >> amount) ? transfer(name, amount, cmd == "RECEIVE") : "FAIL";
    }
    return "UNKNOWN COMMAND";
}

std::string trimLineEnd(std::string text) {
    while (!text.empty() && (text.back() == '\n' || text.back() == '\r')) text.pop_back();
    return text;
}

int main() {
    if (sqlite3_open("../database/banking.db", &db) != SQLITE_OK) {
        std::cerr << "Cannot open database.\n";
        return 1;
    }

    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        sqlite3_close(db);
        return 1;
    }

    SOCKET server = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (server == INVALID_SOCKET) {
        WSACleanup();
        sqlite3_close(db);
        return 1;
    }

    sockaddr_in address = {};
    address.sin_family = AF_INET;
    address.sin_port = htons(9090);
    address.sin_addr.s_addr = INADDR_ANY;

    if (bind(server, reinterpret_cast<sockaddr*>(&address), sizeof(address)) == SOCKET_ERROR ||
        listen(server, 1) == SOCKET_ERROR) {
        closesocket(server);
        WSACleanup();
        sqlite3_close(db);
        return 1;
    }

    std::cout << "Server listening on port 9090...\n";

    while (true) {
        SOCKET client = accept(server, nullptr, nullptr);
        if (client == INVALID_SOCKET) continue;

        char buffer[1024] = {};
        int bytes = recv(client, buffer, sizeof(buffer) - 1, 0);

        if (bytes > 0) {
            std::string response = handleCommand(trimLineEnd(std::string(buffer, bytes)));
            send(client, response.c_str(), static_cast<int>(response.size()), 0);
        }

        closesocket(client);
    }
}