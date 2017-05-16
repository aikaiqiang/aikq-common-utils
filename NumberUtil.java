import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.alibaba.dubbo.rpc.filter.GenericImplFilter;


public class NumberUtil {
	
	public static List<BigDecimal> divide(BigDecimal d, int n) {
		
		List<BigDecimal> res = new ArrayList<BigDecimal>();
		if (d == null || d.compareTo(BigDecimal.ZERO) == 0) {
			return res;
		}
		
		if (n <= 1) {
			res.add(d);
			return res;
		}
		
		BigDecimal per = d.divide(new BigDecimal(n), 3, BigDecimal.ROUND_DOWN);
		BigDecimal tail = d.subtract(per.multiply(new BigDecimal(n)));
		
		for (int i = 0; i < n - 1; i++) {
			res.add(per);
		}
		res.add(per.add(tail));
		
		return res;
	}
	
	public static void main(String[] args) {
		
		System.out.println(divide(new BigDecimal(10), 5));
		System.out.println(divide(new BigDecimal(10), 0));
		System.out.println(divide(new BigDecimal(10), 3));
		
	}

}
