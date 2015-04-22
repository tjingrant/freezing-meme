import java.util.Random;


public class Drive {

	public static void main(String[] args) {
		
		final LinkedSortedList lsl = new LinkedSortedList();
		lsl.Insert(5);
//		System.out.print(lsl.Contains(5));
//		lsl.Delete(5);
//		System.out.print(lsl.Contains(5));
//		System.out.print(lsl.invariantCheck());
		
		lsl.printOut();
		for (int j=0; j<5; j++) {
			(new Thread() {
				  public void run() {
					Random generator = new Random();
				    for(int i=0; i<5; i++) {
					  lsl.Insert(generator.nextInt(10));
					  //System.out.print(lsl.Contains(generator.nextInt(10)) + " \n");
				    }
				  }
			}).start();
		}
		
		lsl.hybernate = true;
		
		try {
			Thread.sleep(1000*3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print(lsl.invariantCheck());
		
		
//		int abc = 0;
//		long start = System.nanoTime();
//		for(int i=0; i<10; i++) {
//			abc=abc+1;
//		}
//		long end = System.nanoTime();
//		System.out.println(end - start);
	}

}
