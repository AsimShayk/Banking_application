PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id integer primary key autoincrement,
    full_name text not null,
    email text not null,
    password_hash text not null,
    account_no text not null unique,
    balance decimal not null default 0.0,
    created_at datetime default current_timestamp
);

CREATE TABLE IF NOT EXISTS beneficiaries (
    id integer primary key autoincrement,
    ben_name text not null,
    account_no text not null unique,
    ifsc_code text not null,
    created_at datetime default current_timestamp
);

CREATE TABLE IF NOT EXISTS transactions (
    id integer primary key autoincrement,
    transaction_type text not null,
    party_name text not null,
    amount decimal not null,
    created_at datetime default current_timestamp
); 

INSERT OR IGNORE INTO beneficiaries (ben_name, account_no, ifsc_code)
VALUES ('Rahul Shah', '40012', 'SBI54000987');