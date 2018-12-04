package net.quanthome;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.quanthome.trade.CoreServiceNew;

@SpringBootApplication
public class QuantTradeApplication {

	public static void main(String[] args) {
		/*SpringApplication.run(QuantTradeApplication.class, args);
		String apiKey = args[1];
	    String secretKey = args[2];
	    String symbol = args[3];
	    String amount = args[4];
	    double backIncome = Double.valueOf(args[5]);
	    boolean isDouble = Boolean.valueOf(args[6]);
	    double span = Double.valueOf(args[7]);
	    int startTime = Integer.valueOf(args[8]);
	    int endTime = Integer.valueOf(args[9]);
	    System.out.println(Arrays.toString(args));
	    new Thread(new CoreService(apiKey, secretKey, symbol, amount, backIncome, isDouble, span, startTime, endTime)).start();
	}*/
		SpringApplication.run(QuantTradeApplication.class, args);
		String apiKey = args[1];
	    String secretKey = args[2];
	    String symbol = args[3];
	    String contractType = args[4];
	    String amount = args[5];
	    double openRate = Double.valueOf(args[6]);
	    double backIncome = Double.valueOf(args[7]);
	    boolean isDouble = Boolean.valueOf(args[8]);
	    double span = Double.valueOf(args[9]);
	    int startTime = Integer.valueOf(args[10]);
	    int endTime = Integer.valueOf(args[11]);
	    System.out.println(Arrays.toString(args));
	    new Thread(new CoreServiceNew(apiKey, secretKey, symbol, contractType, amount, openRate, backIncome, isDouble, span, startTime, endTime)).start();
	}
	
}
