package ro.cs.tao.persistence;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ro.cs.tao.persistence.config.DatabaseConfiguration;

import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Just for test purpose
 */
public class MainApp {

    private static final Logger logger = LogManager.getLogManager().getLogger("");

	public static void main(String[] args) {

		ApplicationContext context = new ClassPathXmlApplicationContext("tao-persistence-context.xml");

		DatabaseConfiguration dbConfig = context.getBean(DatabaseConfiguration.class);
		
		if (dbConfig != null)
		{
			logger.info("DatabaseConfiguration loaded!");
		}

		((ClassPathXmlApplicationContext)context).close();
		logger.info("ApplicationContext closed.");
	}
}
