import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedSortedList {
	boolean debug = false;
	boolean destructiveChecking = false;
	boolean hybernate = false;
	Node start;
	public enum Methods{
		DLSL_INSERT, DLSL_CONTAIN, DLSL_DELETE;
	}
	
	public class Verifier extends HashMap<Integer, Integer> {
		private static final long serialVersionUID = 6759448364384927844L;
	}
	
	public void checkAndHybernate() {
		if (hybernate) {
			try {
				Thread.sleep(1000*100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public class Node {
		private int _a;
		private Node _prev;
		private Node _next;
		AtomicInteger _ver;
		public Node(int a, Node prev, Node next) {
			_a = a; _prev = prev; _next = next; _ver = new AtomicInteger(0);
		}
		
		public Node getPrev() {
			return _prev;
		}
		
		public void setPrev(Node n) {
			_prev = n;
		}
		
		public Node getNext() {
			return _next;
		}
		
		public void setNext(Node n) {
			_next = n;
		}
		
		public int getInt() {
			return _a;
		}
		
		public void setInt(int a) {
			_a = a;
		}
	}
	
	public class TemporaryResult {
		Verifier _verifier;
		boolean b_res;
		int i_res;
		boolean _completed;
		
		public TemporaryResult(boolean completed) {
			_verifier = new Verifier();
			_completed = completed;
		}
		
		public TemporaryResult(boolean val, boolean completed) {
			_verifier = new Verifier();
			b_res = val;
			_completed = completed;
		}
	}
	
	public LinkedSortedList() {
		this.start = new Node(Integer.MIN_VALUE, null, null);
	}
	
	public void Insert(int a) {
		TemporaryResult tr = new TemporaryResult(false);
		int counter = 0;
		while(!tr._completed) {
			tr = this._Insert(a);
			counter ++;
			if (debug && (counter > 0)) {
				System.out.println("INSERT restart due to failed transaction");
			}
			if (counter > 100) {
				System.err.append("Unfair senario occurs");
			}
		}
	}
	
    public TemporaryResult _Insert(int a) {
    	checkAndHybernate();
		Node now = this.start;
    	Node next = this.start.getNext();

    	Node newNode = new Node(a, null, null);
    	
    	while (next != null && !( (now.getInt() <= a) && (next.getInt() >= a) )) {
    		now = next;
        	next = next.getNext();
    	}
    	if (next == null) {
    		int ver = now._ver.get();
    		if ((ver % 2) == 1) {
    			return new TemporaryResult(false);
    		} else if (!now._ver.compareAndSet(ver, ver+1)) {
    			return new TemporaryResult(false);
    		}

    		//at this point, now is at hand
    		now.setNext(newNode);
			newNode.setPrev(now);
			now._ver.incrementAndGet();
			return new TemporaryResult(true);
		}
    	if ( (now.getInt() <= a) && (next.getInt() >= a) ) {
    		int ver = now._ver.get();
    		if ((ver % 2) == 1) {
    			return new TemporaryResult(false);
    		} else if (!now._ver.compareAndSet(ver, ver+1)) {
    			return new TemporaryResult(false);
    		}
    		// at this point, now is claimed
    		ver = next._ver.get();
    		if ((ver % 2) == 1) {
    			now._ver.decrementAndGet();
    			return new TemporaryResult(false);
    		} else if (!next._ver.compareAndSet(ver, ver+1)) {
    			now._ver.decrementAndGet();
    			return new TemporaryResult(false);
    		}
    		// at this point, now and next are all claimed
    		now.setNext(newNode);
    		newNode.setPrev(now);
    		next.setPrev(newNode);
    		newNode.setNext(next);
    		now._ver.incrementAndGet(); //becomes even, relinquishing control
    		next._ver.incrementAndGet(); //becomes even, relinquishing control
    		return new TemporaryResult(true);
    	}
    	return new TemporaryResult(false);
    }
	/** marks two consecutive atomic integers as dirty, if either of them are already dirty
	 *  fail right away.
	 */
    // have to make use of hashcode
    public boolean Contains(int val) {
		TemporaryResult tr = new TemporaryResult(false, false);
		int counter = 0;
		while(!tr._completed) {
			tr = this._Contains(val);
			counter ++;
			if (debug && (counter > 0)) {
				System.out.println("CONTAINS restart due to failed transaction");
			}
			if (counter > 100) {
				System.err.append("Unfair senario occurs");
			}
		}
		return tr.b_res;
    }
    
	public TemporaryResult _Contains(int val) {   //valid only for positive tests, negative test may compromise concurrency significantly
		checkAndHybernate();
		Node next = this.start;
    	while(next != null) {
    		int ver = next._ver.get();
    		if ((ver % 2) == 1) {       //someone is manipulating it
    			return new TemporaryResult(false);
    		}
    		if ((val == next.getInt())) {
    			TemporaryResult tr = new TemporaryResult(true, true);
    			tr._verifier.put(next.hashCode(), ver);
    			return tr;
    		}
    		next = next.getNext();
    	}
    	return new TemporaryResult(false, true);
	}

	public void Delete(int val) {
		TemporaryResult tr = new TemporaryResult(false);
		int counter = 0;
		while(!tr._completed) {
			tr = this._Delete(val);
			counter ++;
			if (debug && (counter > 0)) {
				System.out.println("DELETE restart due to failed transaction");
			}
			if (counter > 100) {
				System.err.append("Unfair senario occurs");
			}
		}
	}
	
	public TemporaryResult _Delete(int val) {
		checkAndHybernate();
		Node next = this.start;
		while(next != null) {
			if (next.getNext() == null) {
				int ver = next.getPrev()._ver.get();
	    		if ((ver % 2) == 1) {
	    			return new TemporaryResult(false);
	    		} else if (!next.getPrev()._ver.compareAndSet(ver, ver+1)) {
	    			return new TemporaryResult(false);
	    		}
				next.getPrev().setNext(null);
				next.getPrev()._ver.incrementAndGet();
				return new TemporaryResult(true);
			}
			if (val == next.getInt()) {
				Node p = next.getPrev();
				Node n = next.getNext();
				int ver = n._ver.get();
	    		if ((ver % 2) == 1) {
	    			return new TemporaryResult(false);
	    		} else if (!n._ver.compareAndSet(ver, ver+1)) {
	    			return new TemporaryResult(false);
	    		}
	    		// at this point, now is claimed
	    		ver = p._ver.get();
	    		if ((ver % 2) == 1) {
	    			n._ver.decrementAndGet();
	    			return new TemporaryResult(false);
	    		} else if (!p._ver.compareAndSet(ver, ver+1)) {
	    			n._ver.decrementAndGet();
	    			return new TemporaryResult(false);
	    		}
	    		// at this point, now and next are all claimed
				p.setNext(n);
				n.setPrev(p);
				n._ver.incrementAndGet();
				p._ver.incrementAndGet();
				return new TemporaryResult(true);
			}
			next = next.getNext();
	   }
	   return new TemporaryResult(true); //okay if it doesn't exist
	}
	
    public void printOut() {
    	Node next = this.start.getNext();
    	while(next != null) {
    		System.out.print(next.getInt() + " ");
    		next = next.getNext();
    	}
    }
    
	public boolean invariantCheck() {
		Node now = this.start;
    	Node next = this.start.getNext();
    	boolean inv = true;
    	
    	while (next != null) {
    	  inv = inv && (now._a <= next._a);	
    	  now = next;
    	  next = next.getNext();
    	}
    	
		return true;
	}
	
}

/** each node is associated with an atomicInteger which records the latest "clean state"
 *  A txnal acquire records that atomicInteger's val
 *  A txnal release checks if that val has changed and if it has, then critical section should be re-performed.
 *  Even number state - complete clean state
 *  Odd number state - unstable state
 **/
