import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Properties;

public class BankingAppSingleFile {

    // Account Entity
    @Entity
    public static class Account {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        private String name;
        private double balance;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getBalance() { return balance; }
        public void setBalance(double balance) { this.balance = balance; }

        @Override
        public String toString() {
            return "Account{id=" + id + ", name='" + name + "', balance=" + balance + "}";
        }
    }

    // TransactionRecord Entity
    @Entity
    public static class TransactionRecord {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        private int fromAccountId;
        private int toAccountId;
        private double amount;
        private LocalDateTime timestamp;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(int fromAccountId) { this.fromAccountId = fromAccountId; }

        public int getToAccountId() { return toAccountId; }
        public void setToAccountId(int toAccountId) { this.toAccountId = toAccountId; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // Spring configuration and Hibernate setup
    @Configuration
    @EnableTransactionManagement
    public static class AppConfig {
        @Bean
        public SessionFactory sessionFactory() {
            Properties props = new Properties();
            props.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            props.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/your_db");
            props.setProperty("hibernate.connection.username", "root");
            props.setProperty("hibernate.connection.password", "your_password");
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
            props.setProperty("hibernate.hbm2ddl.auto", "update");
            props.setProperty("hibernate.show_sql", "true");

            Configuration configuration = new Configuration().addProperties(props);
            configuration.addAnnotatedClass(Account.class);
            configuration.addAnnotatedClass(TransactionRecord.class);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();

            return configuration.buildSessionFactory(serviceRegistry);
        }

        @Bean
        public HibernateTransactionManager transactionManager() {
            return new HibernateTransactionManager(sessionFactory());
        }

        @Bean
        public BankService bankService() {
            return new BankService(sessionFactory());
        }
    }

    // BankService class to handle transactions
    public static class BankService {

        private SessionFactory factory;

        public BankService(SessionFactory factory) {
            this.factory = factory;
        }

        public void transferMoney(int fromId, int toId, double amount) {
            Session session = factory.openSession();
            Transaction tx = session.beginTransaction();

            try {
                Account from = session.get(Account.class, fromId);
                Account to = session.get(Account.class, toId);

                if (from.getBalance() < amount) {
                    throw new RuntimeException("❌ Insufficient Balance");
                }

                from.setBalance(from.getBalance() - amount);
                to.setBalance(to.getBalance() + amount);

                session.update(from);
                session.update(to);

                TransactionRecord t = new TransactionRecord();
                t.setFromAccountId(fromId);
                t.setToAccountId(toId);
                t.setAmount(amount);
                t.setTimestamp(LocalDateTime.now());

                session.save(t);

                tx.commit();
                System.out.println("✅ Transaction Successful!");
            } catch (Exception e) {
                tx.rollback();
                System.out.println("🔁 Transaction Rolled Back: " + e.getMessage());
            } finally {
                session.close();
            }
        }
    }

    // Main method to run the example
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        BankService bankService = context.getBean(BankService.class);

        // Test case 1: Successful transaction
        bankService.transferMoney(1, 2, 1000);

        // Test case 2: Failed transaction (insufficient balance)
        bankService.transferMoney(1, 2, 10000);

        context.close();
    }
}
