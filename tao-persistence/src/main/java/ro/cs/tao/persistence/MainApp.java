package ro.cs.tao.persistence;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ro.cs.tao.persistence.config.DatabaseConfiguration;

public class MainApp {
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("tao-persistence-context.xml");

		DatabaseConfiguration dbConfig = (DatabaseConfiguration) context.getBean(DatabaseConfiguration.class);
		
		if (dbConfig != null)
		{
			System.out.println("DatabaseConfiguration loaded!");
		}

		((ClassPathXmlApplicationContext)context).close();
		System.out.println();

	}
}
