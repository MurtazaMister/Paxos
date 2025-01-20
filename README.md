# Distributed Banking System with Paxos Protocol

Welcome to the **Distributed Banking System** repository! This project demonstrates the implementation of a **variant of the Paxos Consensus Protocol**, applied to a distributed banking system. The system achieves consistency, fault tolerance, and reliability across multiple nodes, even in the face of failures.

---

## 📝 **Overview**

This project is built to simulate a distributed environment where banking transactions are processed and replicated across **5 nodes**, ensuring data consistency through the Paxos consensus algorithm. The system can tolerate up to **2 node failures** without impacting correctness.

---

## ⚙️ **Features**

- **Node Failure Tolerance:** Handles up to 2 failures in a 5-node system.  
- **Local and Distributed Transactions:** Supports both local transactions and Paxos-based consensus for distributed transactions.  
- **Block Construction:** Combines multiple transactions into a block for efficiency.  
- **High Performance:** Optimized for minimal latency (0.6 seconds per transaction) and high throughput (1.5 transactions/second).  

---

## 🚀 **Getting Started**

### Prerequisites
- Java 17+
- Spring Boot
- Maven

### Setup Instructions
1. Clone this repository:  
   ```bash
   git clone https://github.com/MurtazaMister/Paxos.git
   cd paxos
   ```
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   run_servers.bat
   ```

## 🛠️ How It Works

1. **Client-Server Architecture:** Each client is connected to a dedicated server for initiating transactions.
2. **Local Transactions:** Transactions are processed locally if sufficient balance exists.
3. **Consensus via Paxos:** For insufficient local balance, the Paxos protocol ensures global consistency by:
   - Collecting local transaction data from all servers.
   - Achieving consensus on a constructed transaction block.
   - Broadcasting the committed block to all nodes.

---

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your improvements.

---

## 📧 Contact

For queries or feedback, reach out at:  
📬 **murtazaakil.mister@stonybrook.edu**
