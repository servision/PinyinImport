import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinImport {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String appId = "weichaiEpc";
		Map<String, String> parts = getParts();
		generateAndSaveI18NKey(parts);
		generateAndSavePinyin(parts,appId);
	}

	private static Map<String, String> getParts() {
		Map<String, String> map = new HashMap<String, String>();
		Connection connection = null;
		try {
			connection = getConnection();
			try {
				Statement stmt = connection.createStatement();
				String selectPartQuery = "select * from EPC_PART";
				ResultSet rs = stmt.executeQuery(selectPartQuery);
				while (rs.next()) {
					map.put(rs.getString("part_number"),
							rs.getString("part_description"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;

	}

	private static void generateAndSaveI18NKey(Map<String, String> parts) {

		Connection connection = null;
		try {
			connection = getConnection();
			String query = "update EPC_PART set I18N_KEY = ? where PART_NUMBER = ?";
			PreparedStatement stmt = connection.prepareStatement(query);
			try {
				for (String partNumber : parts.keySet()) {
					String i18n_key = String.valueOf(generateI18NKey("Part"	+ "." + partNumber +"."+ parts.get(partNumber)));

					stmt.setString(1, i18n_key);
					stmt.setString(2, partNumber);
					stmt.executeUpdate();
				}
				

			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void generateAndSavePinyin(Map<String, String> parts,
			String appId) {

		Connection connection = null;
		try {
			connection = getConnection();
			String query = "insert into I18N_PINYIN(APPID,KEY,FULL,ABBR) values(?,?,?,?)";
			PreparedStatement stmt = connection.prepareStatement(query);
			try {
				for (String partNumber : parts.keySet()) {
					String partName = parts.get(partNumber);
					String i18n_key = String.valueOf(generateI18NKey("Part"	+ "." + partNumber +"."+ parts.get(partNumber)));
                    String fullPinyin = getFullPinyin(partName);
                    String pinyinAbbr = getPinyinAbbr(partName);
					stmt.setString(1, appId);
					stmt.setString(2, i18n_key);
					stmt.setString(3, fullPinyin);
					stmt.setString(4, pinyinAbbr);
					stmt.executeUpdate();
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String getFullPinyin(String chinese) {
		if (chinese == null) {  
            return null;  
        }  
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();  
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 小写  
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 不标声调  
        format.setVCharType(HanyuPinyinVCharType.WITH_V);// u:的声母替换为v  
        try {  
            StringBuilder sb = new StringBuilder();  
            for (int i = 0; i < chinese.length(); i++) {  
                String[] array = PinyinHelper.toHanyuPinyinStringArray(chinese  
                        .charAt(i), format);  
                if (array == null || array.length == 0) {  
                    continue;  
                }  
                String s = array[0];// 不管多音字,只取第一个  
                char c = s.charAt(0);// 小写第一个字母  
                String pinyin = String.valueOf(c).toLowerCase().concat(s  
                        .substring(1));  
                sb.append(pinyin);  
            }  
            return sb.toString();  
        } catch (BadHanyuPinyinOutputFormatCombination e) {  
            e.printStackTrace();  
        }  
        return null;  
	}
	
	public static String getPinyinAbbr(String chinese) {  
        if (chinese == null) {  
            return null;  
        }  
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();  
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 小写  
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 不标声调  
        format.setVCharType(HanyuPinyinVCharType.WITH_V);// u:的声母替换为v  
        try {  
            StringBuilder sb = new StringBuilder();  
            for (int i = 0; i < chinese.length(); i++) {  
                String[] array = PinyinHelper.toHanyuPinyinStringArray(chinese  
                        .charAt(i), format);  
                if (array == null || array.length == 0) {  
                    continue;  
                }  
                String s = array[0];// 不管多音字,只取第一个  
                char c = s.charAt(0);// 小写第一个字母  
                String pinyinAbbr = String.valueOf(c).toLowerCase();
                sb.append(pinyinAbbr);  
            }  
            return sb.toString();  
        } catch (BadHanyuPinyinOutputFormatCombination e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  

	private static int generateI18NKey(String partName) {
		return Math.abs(partName.hashCode());
	}

	private static Connection getConnection() {
		Connection conn = null;
		String url = "jdbc:oracle:thin:@srv-data:1521:";
		String dbName = "orcl";
		String driverName = "oracle.jdbc.OracleDriver";
		String userName = "scott";
		String password = "tiger";
		try {
			Class.forName(driverName).newInstance();
			conn = DriverManager
					.getConnection(url + dbName, userName, password);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

}
