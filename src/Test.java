import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Map;
public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		Map<String, String> m = new TreeMap<String, String>();
//		m.put("A1", "A1");
//		m.put("A2", "A2");
//		m.put("B1", "B1");
//		m.put("B2", "B2");
//		m.put("C1", "C1");
//		m.put("C2", "C2");
//		m.remove("A2");
		
			LinkedHashMap<String,String> lhm = new LinkedHashMap<String, String>();
			lhm.put("1", "1");
			lhm.put("2", "2");
			lhm.put("3", "3");
			lhm.put("4", "4");
			lhm.put("5", "5");
			lhm.put("6", "6");
			for(int i=lhm.keySet().size();i>0;i--){
			System.out.print(i);
			System.out.println("=:"+lhm.get(String.valueOf(i)));
			}
			
//		Iterator<String> iterator = m.keySet().iterator();
//		 
//		while (iterator.hasNext()) {
//		    String key = iterator.next();
//		    if (key.startsWith("B")) {
//		        iterator.remove();
//		    }
//		}
		 
//		for (String key : m.keySet()) {
//		    System.out.println(key + ":" + m.get(key));
//		}
//		Map<Integer, String> map = new HashMap<>();
//
//		map.put(1, "value 1");
//
//		map.put(2, "value 2");
//
//		map.put(3, "value 3");
	}
//	Map<String, String> paramMap = new HashMap<String, String>();
//    paramMap.put("A1", "A1");
//    paramMap.put("A2", "A2");
//    paramMap.put("B1", "B1");  
//    paramMap.put("B2", "B2");  
//    paramMap.put("C1", "C1");  
//    paramMap.put("C2", "C2");
//    System.out.println(paramMap);
//     
//    Iterator<String> iter = paramMap.keySet().iterator();
//    while (iter.hasNext()) {
//        String key = iter.next();
//        if (key.startsWith("B")) {
//            iter.remove();
//            paramMap.remove(key);
//        }
//    }
//    System.out.println(paramMap);

}
