import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringTaskTest {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-springTask.xml");
	}
}
