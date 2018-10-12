package net.quanthome;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.quanthome.trade.CoreService;

@SpringBootApplication
public class QuantTradeApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuantTradeApplication.class, args);
		String apiKey = args[1];
	    String secretKey = args[2];
	    String symbol = args[3];
	    String amount = args[4];
	    double backIncome = Double.valueOf(args[5]);
	    boolean isDouble = Boolean.valueOf(args[6]);
	    int startTime = Integer.valueOf(args[7]);
	    int endTime = Integer.valueOf(args[7]);
	    System.out.println(Arrays.toString(args));
	    new Thread(new CoreService(apiKey, secretKey, symbol, amount, backIncome, isDouble, startTime, endTime)).start();
	}
	
}
