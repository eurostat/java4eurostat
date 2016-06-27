/**
 * 
 */
package eu.ec.java4eurostat.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * 
 * Some generic functions to manipulate EuroBase timestamps
 * 
 * @author julien Gaffuri
 *
 */
@Deprecated
public class EBTimeUtil {

	public static boolean isMonth(String hICPMDate) { return hICPMDate.contains("M"); }
	public static boolean isYear(String hICPMDate) { return !isMonth(hICPMDate); }

	public static String getEBText(int year, int month) { return year+"M"+(month<10?"0":"")+month+" "; }
	public static String getEBText(int year) { return year+" "; }

	public static String getYear(String HICPMDate){
		return new StringTokenizer(HICPMDate.replaceAll(" ", ""),"M").nextToken();
	}
	public static int getYearInt(String HICPMDate) {
		return Integer.parseInt(getYear(HICPMDate));
	}
	public static String getMonth(String HICPMDate){
		StringTokenizer st = new StringTokenizer(HICPMDate,"M"); st.nextToken();
		return st.nextToken().replace(" ", "");
	}
	public static int getMonthInt(String HICPMDate) {
		return Integer.parseInt(getMonth(HICPMDate));
	}

	public static String get(String HICPMDate, int nb) {
		if(isYear(HICPMDate)){
			return (getYearInt(HICPMDate)+nb)+" ";
		} else if (isMonth(HICPMDate)) {
			int year = getYearInt(HICPMDate) + ((int)nb/12);
			int month = getMonthInt(HICPMDate) + nb%12;

			if(month<1) {month+=12;year--;}
			if(month>12) {month-=12;year++;}

			return getEBText(year, month);
		}
		System.err.println("Unhandled date format: "+HICPMDate);
		return null;
	}

	public static String getPrevious(String HICPMDate){ return get(HICPMDate, -1); }
	public static String getNext(String HICPMDate){ return get(HICPMDate, 1); }
	public static String getDecPrevYear(String time) { return (getYearInt(time)-1) + "M12 "; }

	public static int getDuration(String date1, String date2) {
		if(isYear(date1) && isYear(date2))
			return getYearInt(date2) - getYearInt(date1);
		else if(isMonth(date1) && isMonth(date2))
			return getMonthInt(date2)-getMonthInt(date1) + 12*(getYearInt(date2)-getYearInt(date1));
		System.err.println("Unhandled date format: "+date1+" "+date2);
		return 0;
	}

	public static Comparator<String> HICPMDateComparator = new Comparator<String>(){
		@Override
		public int compare(String time1, String time2) { return -getDuration(time1, time2); }
	};
	public static boolean isStrictlyBefore(String time1, String time2) { return HICPMDateComparator.compare(time1, time2) < 0; }


	public static ArrayList<Integer> getYears(Set<String> times) {
		HashSet<Integer> years = new HashSet<Integer>();
		for(String time : times) years.add(EBTimeUtil.getYearInt(time));
		ArrayList<Integer> years_ = new ArrayList<Integer>(years);
		Collections.sort(years_);
		return years_;
	}

}
