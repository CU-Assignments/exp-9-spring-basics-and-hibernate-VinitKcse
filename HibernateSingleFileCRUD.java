import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import jakarta.persistence.*;

import java.util.Properties;

public class HibernateSingleFileCRUD {

    @Entity
    @Table(name = "student")
    public static class Student {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;
        private String name;
        private int age;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        @Override
        public String toString() {
            return "Student{id=" + id + ", name='" + name + "', age=" + age + "}";
        }
    }

    private static SessionFactory buildSessionFactory() {
        Properties props = new Properties();
        props.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        props.put("hibernate.connection.url", "jdbc:mysql://localhost:3306/your_db");
        props.put("hibernate.connection.username", "root");
        props.put("hibernate.connection.password", "your_password");
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");

        Configuration cfg = new Configuration();
        cfg.setProperties(props);
        cfg.addAnnotatedClass(Student.class);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(cfg.getProperties()).build();

        return cfg.buildSessionFactory(serviceRegistry);
    }

    public static void main(String[] args) {
        SessionFactory sessionFactory = buildSessionFactory();

        // CREATE
        Student s1 = new Student();
        s1.setName("John");
        s1.setAge(22);

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(s1);
        tx.commit();
        session.close();
        System.out.println("Created: " + s1);

        // READ
        session = sessionFactory.openSession();
        Student student = session.get(Student.class, s1.getId());
        session.close();
        System.out.println("Read: " + student);

        // UPDATE
        student.setAge(25);
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        session.update(student);
        tx.commit();
        session.close();
        System.out.println("Updated: " + student);

        // DELETE
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        session.delete(student);
        tx.commit();
        session.close();
        System.out.println("Deleted: " + student);

        sessionFactory.close();
    }
}
