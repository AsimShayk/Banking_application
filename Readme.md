A simple banking application that uses Java for frontend, C++ for backend, and SQLite for the database





compile-

javac -cp .;sqlite-jdbc-3.53.1.0.jar Main.java DBSetup.java LoginFrame.java DashboardFrame.java SendMoneyFrame.java ReceiveMoneyFrame.java AddBeneficiaryFrame.java TransactionsFrame.java



run-

java -cp .;sqlite-jdbc-3.53.1.0.jar Main



server.exe-

gcc -c sqlite3.c \&\& g++ server.cpp sqlite3.o -o server -lws2\_32

