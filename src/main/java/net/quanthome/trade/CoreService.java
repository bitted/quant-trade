package net.quanthome.trade;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import net.quanthome.entity.OrderInfo;
import net.quanthome.entity.Position;
import net.quanthome.trade.okex.QuotServiceImpl;
import net.quanthome.trade.okex.TradeServiceImpl;

public class CoreService implements Runnable {
	
	private static final String CONTRACT_TYPE = "quarter";
	
	private TradeServiceImpl tradeServiceImpl = null;

	private QuotServiceImpl quotServiceImpl = null;
	
	private String amount = null;

	private double backIncome = 0.0D;

	private double maxIncome = 0.0D;
	
	private boolean isDouble = false;
	
	private double count = 1;
	
	private double multiple = 2.0;
	
	private int startTime = 1;
	
	private int endTime = 1;
	
	private double span = 0.1;

	public CoreService(String api_key, String secret_key, String symbol, String amount, double backIncome, boolean isDouble, double span, int startTime, int endTime) {
		this.quotServiceImpl = new QuotServiceImpl(symbol, CONTRACT_TYPE);
		this.tradeServiceImpl = new TradeServiceImpl(api_key, secret_key, symbol, CONTRACT_TYPE);
		this.amount = amount;
		this.backIncome = backIncome;
		this.isDouble = isDouble;
		this.startTime = startTime;
		this.endTime = endTime;
		this.span = span;
	}

	public void run() {
		while (true) {
			try{
				System.out.println("发起新交易----------------------------！");
				//先查看是否有仓位
				Position position = tradeServiceImpl.position();
				if(position!=null && (position.getBuy_amount()>0 || position.getSell_amount()>0) ){
					System.out.println("当前有仓位，进行仓位监控！");
					monitorQuick();
				}
				
				System.out.println("当前无仓位，开多仓！");
				//判断是否是指定的时间段
				
				double last = this.quotServiceImpl.ticker().getLast();
				
				//判断是否在开仓时间段
				if(openPositionTime()) {
					this.tradeServiceImpl.slip(60000);//暂停1分钟
					continue;
				}
				String orderId = this.tradeServiceImpl.trade(this.tradeServiceImpl.getOpenPrice("1", last), amount, "1");
				if(orderId==null) {
					System.out.println("下单失败!");
					continue;
				}
				orderMonitor(orderId);
				System.out.println("多仓开仓成功！");
				monitorQuick();
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private boolean openPositionTime() {
		if(startTime==endTime) {
			return false;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));//指定时区
		int hour = Integer.parseInt(sdf.format(Calendar.getInstance().getTime()));
		if(hour<startTime || hour>endTime) {
			sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
			sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));//指定时区
			System.out.println("不在开仓时间段，不开仓!"+startTime+","+endTime+","+sdf.format(Calendar.getInstance().getTime()));
			return true;
		}else {
			return false;
		}
	}
	
	
	private void monitorQuick() {
			System.out.println("开始监控仓位！");
			while (true) {
				try {
					this.tradeServiceImpl.slip(1000*5);
	
					Position position = this.tradeServiceImpl.position();
					
					if(position==null){
						System.out.println("仓位为空!");
						break;
					}
					
					double buyLossratio = position.getBuy_profit_lossratio();
					int buyAmount = position.getBuy_amount();
	
					double sellLossratio = position.getSell_profit_lossratio();
					int sellAmount = position.getSell_amount();
	
					if (buyAmount > 0) {
						if (buyLossratio > this.maxIncome) {
							this.maxIncome = buyLossratio;
							System.out.println("多仓收益大于当前最大收益["+buyLossratio+"]");
						} else if(buyLossratio+backIncome < this.maxIncome){
							System.out.println("收益回调达到指定比例，进行平仓操作["+buyLossratio+"]");
							double last = this.quotServiceImpl.ticker().getLast();
							String orderid = this.tradeServiceImpl.trade(this.tradeServiceImpl.getOpenPrice("3", last), buyAmount + "", "3");
							if(orderid==null) {
								System.out.println("平多仓失败!");
								continue;
							}
							orderMonitor(orderid);
							this.maxIncome = 0.0D;
							if(buyLossratio > 10){
								count = 1;
								multiple = 2;
							}else{
								count *= multiple;
								multiple -= span;
								multiple = multiple < 1 ? 1 : multiple;
							}
							System.out.println("平多仓成功，开空仓！");
							last = this.quotServiceImpl.ticker().getLast();
							//判断是否在开仓时间段
							if(openPositionTime()) {
								break;
							}
							System.out.println("开仓张数："+String.valueOf(count*Integer.valueOf(amount)));
							orderid = this.tradeServiceImpl.trade(this.tradeServiceImpl.getOpenPrice("2", last), isDouble?String.valueOf(count*Integer.valueOf(amount)):amount, "2");
							if(orderid==null) {
								System.out.println("开空仓失败！");
								continue;
							}
							orderMonitor(orderid);
							System.out.println("空仓开仓成功！");
							monitorQuick();
						}
					} else if (sellAmount > 0){
						if (sellLossratio > this.maxIncome) {
							this.maxIncome = sellLossratio;
							System.out.println("持仓收益大于当前最大收益["+sellLossratio+"]");
						} else if(sellLossratio+backIncome < this.maxIncome){
							System.out.println("收益回调达到指定比例，进行平仓操作["+sellLossratio+"]");
							double last = this.quotServiceImpl.ticker().getLast();
							String orderid = this.tradeServiceImpl.trade(this.tradeServiceImpl.getOpenPrice("4", last), sellAmount + "", "4");
							if(orderid==null) {
								System.out.println("平空仓失败！");
								continue;
							}
							orderMonitor(orderid);
							this.maxIncome = 0.0D;
							if(sellLossratio > 10){
								count = 1;
								multiple = 2;
							}else{
								count *= multiple;
								multiple -= span;
								multiple = multiple < 1 ? 1 : multiple;
							}
							System.out.println("平空仓成功，开多仓！");
							last = this.quotServiceImpl.ticker().getLast();
							//判断是否在开仓时间段
							if(openPositionTime()) {
								break;
							}
							System.out.println("准备开仓张数："+String.valueOf(count*Integer.valueOf(amount)));
							orderid = this.tradeServiceImpl.trade(this.tradeServiceImpl.getOpenPrice("1", last), isDouble?String.valueOf(count*Integer.valueOf(amount)):amount, "1");
							if(orderid==null) {
								System.out.println("开多仓失败！");
								continue;
							}
							orderMonitor(orderid);
							System.out.println("多仓开仓成功！");
							monitorQuick();
						} 
					} else {
						System.out.println("当前无持仓！");
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("监控仓位失败!");
				}
			}
			
	}
	
	
	private void orderMonitor(String orderId){
		while(true){
			try{
				OrderInfo orderInfo = this.tradeServiceImpl.orderInfo(orderId);
				if(orderInfo!=null && "2".equals(orderInfo.getStatus())){
					return;
				}
				this.tradeServiceImpl.slip(20000);
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	
}