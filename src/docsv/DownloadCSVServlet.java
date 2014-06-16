package docsv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;

import javax.servlet.http.*;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class DownloadCSVServlet extends HttpServlet {
	private static String readAll(BufferedReader rd) throws IOException {
		StringBuilder sb = new StringBuilder();

		String cp = new String();

		while ((cp = rd.readLine()) != null) {

			sb.append(cp);
		}
		return sb.toString();
	}

	public static JSONArray readJsonFromUrl(String url) throws IOException,
			JSONException {
		URL Url = new URL(url);
		/*URLConnection urlConnection = Url.openConnection();
		urlConnection.setConnectTimeout(60000);
		urlConnection.setReadTimeout(60000);*/
		
		InputStream is = Url.openStream();
		
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));

			String jsonText = readAll(rd);

			JSONArray arr = new JSONArray(jsonText);

			// System.out.println(arr.toString(2));

			return arr;
		} finally {
			is.close();
		}
	}

	/*public JSONArray getJson() throws IOException, JSONException {
		JSONArray json = readJsonFromUrl("http://fdacswx.fawn.ifas.ufl.edu/index.php/read/sevendaytimestamp/station_id/"+id+"/format/json/");

		return json;
	}*/
	
	public boolean findValue(String s, String[] values){
		for(String ss : values){
			if(ss.equals(s)){
				return true;
			}
		}
		return false;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition",
				"attachment;filename=\"HistroyData.csv\"");
		String id = request.getParameter("stations");
		String from = request.getParameter("from");
		String to = request.getParameter("to");
		String[] measurements = request.getParameterValues("Measurement");
		int len = measurements.length + 2;
		String[] values = new String[len];
		values[0] = "station_name";
		values[1] = "date_time";
		System.arraycopy(measurements, 0, values, 2, measurements.length);
		
		String url = "http://test.fdacswx.fawn.ifas.ufl.edu/index.php/read/select/station_id/"
				+ id+"/start_date/"+from+"/end_date/"+to+"/format/json/";
		//"http://fdacswx.fawn.ifas.ufl.edu/index.php/read/sevendaytimestamp/station_id/"+ id + "/format/json/";
		
		try {
			JSONArray jsonArray = readJsonFromUrl(url);
			
			int length = jsonArray.length();
			OutputStream outputStream = response.getOutputStream();
			for (int i = 0; i < length; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (i == 0) {
					Iterator itr = jsonObject.keys();
					String firstLine = "";
					String dataLine = "";
					while (itr.hasNext()) {
						String key = (String) itr.next();
						if(findValue(key, values)){
							firstLine += key + ",";
							dataLine += jsonObject.getString(key) + ",";
						}
					}
					firstLine += "\n";
					dataLine += "\n";
					outputStream.write(firstLine.getBytes());
					outputStream.write(dataLine.getBytes());
				} else {
					Iterator itr = jsonObject.keys();
					String dataLine = "";
					while (itr.hasNext()) {
						String key = (String) itr.next();
						if(findValue(key, values)){
							dataLine += jsonObject.getString(key) + ",";
						}
					}
					dataLine += "\n";
					outputStream.write(dataLine.getBytes());
				}

			}

			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			// model.closeConnection();
			System.out.println(e.toString());
		}
	}
}
