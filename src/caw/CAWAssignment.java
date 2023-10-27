package caw;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class CAWAssignment {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\shiva\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		
		driver.get("https://testpages.herokuapp.com/styled/tag/dynamic-table.html");
		driver.manage().window().maximize();
		Thread.sleep(2000);
		
		driver.findElement(By.xpath("//*[text()='Table Data']")).click();

		driver.findElement(By.id("jsondata")).clear();
        
		 // Read data from a text file
        //String filePath = "C:\\Users\\shiva\\Desktop\\input.txt";
		String filePath = "D:\\New folder\\selenium\\seleniumProject\\SeleniumP\\src\\resources\\input.txt";
        String data = readTextFile(filePath);
        WebElement inputElement = driver.findElement(By.id("jsondata"));
        inputElement.sendKeys(data);
        
		Thread.sleep(2000);
		driver.findElement(By.id("refreshtable")).click();
		
		 WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tablehere"))); // Change to your actual table ID

	        // Get the table data
	        WebElement tableElement = driver.findElement(By.id("tablehere")); // Change to your actual table ID
	        String tableData = tableElement.getText();
	        System.out.println(tableData);
	        JsonArray actual = convertTableDataToJson(tableData);

	        // Convert JsonArray to List<Map<String, Object>>
	        List<Map<String, Object>> dataMapWebUi = convertJsonArrayToList(actual);
	        List<Map<String, Object>> actualDataMap  = getActualDataMap(data);
	        
	        System.out.println("dataMapWebUi");
	        System.out.println(dataMapWebUi);
	        
	        System.out.println("\n\n");

	        System.out.println("actualDataMap");
	        System.out.println(actualDataMap);
	        

	        Assert.assertNotNull(actualDataMap);
	        Assert.assertNotNull(dataMapWebUi);
	       
	        Assert.assertEquals(dataMapWebUi.size(), actualDataMap.size());
	        
	        for(int i=0; i< dataMapWebUi.size(); i++) {
	        	Map<String, Object> webUiRecordMap = dataMapWebUi.get(i);
	        	Map<String, Object> actualDataRecordMap = actualDataMap.get(i);
	        	Assert.assertEquals(webUiRecordMap.get("name"), actualDataRecordMap.get("name"));
	        	Assert.assertEquals(webUiRecordMap.get("age"), actualDataRecordMap.get("age"));
	        	Assert.assertEquals(webUiRecordMap.get("gender"), actualDataRecordMap.get("gender"));
	        }
	        
	        System.out.println("\n\n ************************ ALL ASSERTS PASSED ***************************");
	}
	
	private static List<Map<String, Object>> getActualDataMap(String data){
		 ObjectMapper objectMapper = new ObjectMapper();
		 List<Map<String, Object>> dataMap = null;
		 try {
                dataMap = objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {});

	            // Now 'data' contains your JSON records as a List of Maps
	            for (Map<String, Object> record : dataMap) {
	                String name = (String) record.get("name");
	                int age = (int) record.get("age");
	                String gender = (String) record.get("gender");

//	                System.out.println("Name: " + name + ", Age: " + age + ", Gender: " + gender);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		
		 return dataMap;
	}

	private static List<Map<String, Object>> convertJsonArrayToList(JsonArray jsonArray) {
		List<Map<String, Object>> list = new ArrayList<>();
		Gson gson = new Gson();

		 for (JsonElement element : jsonArray) {
	            if (element.isJsonObject()) {
	                JsonObject jsonObject = element.getAsJsonObject();
	                Map<String, Object> map = new HashMap<>();

	                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
	                    String key = entry.getKey();
	                    JsonElement value = entry.getValue();
	                    if (value.isJsonPrimitive() && ((JsonPrimitive) value).isNumber()) {
	                        // If the value is a number, store it as an Integer
	                        map.put(key, value.getAsInt());
	                    } else {
	                        // Otherwise, store it as is
	                        map.put(key, value.getAsString());
	                    }
	                }

	                list.add(map);
	            }
	        }

		return list;
	}

	private static String readTextFile(String filePath) throws IOException {
		StringBuilder content = new StringBuilder();

		File myObj = new File(filePath);
		Scanner myReader = new Scanner(myObj);
		while (myReader.hasNextLine()) {
			String data = myReader.nextLine();
//	        System.out.println(data);
			content.append(data).append("\n");
		}
		myReader.close();
//	      System.out.println(content.toString().replace(" ", ""));

		return content.toString().replace(" ", "");
	}

	private static JsonArray convertTableDataToJson(String tableData) {
		JsonArray jsonArray = new JsonArray();
		String[] rows = tableData.split("\n");
		String[] headers = rows[1].split("\\s+");
		for (int i = 2; i < rows.length; i++) {
			String[] values = rows[i].split("\\s+");
			JsonObject obj = new JsonObject();
			for (int j = 0; j < headers.length; j++) {
				if (isNumeric(values[j])) {
					obj.addProperty(headers[j], Integer.parseInt(values[j]));
				} else {
					obj.addProperty(headers[j], values[j]);
				}
			}
			jsonArray.add(obj);
		}

		return jsonArray;
	}

	private static boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
