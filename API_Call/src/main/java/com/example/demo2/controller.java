package com.example.demo2;

import java.util.*;
import java.io.*;
import java.net.*;
import com.google.common.io.CharStreams;

import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;


@RestController
public class controller {
	private final ObjectMapper mapper = new ObjectMapper();
	private final Map<String,List<ObjectNode>> display= new HashMap<>();
	private final List<ObjectNode> result = new ArrayList<ObjectNode>();
	private String inputLine=null,organization_id=null;

	@RequestMapping(value= {"/repos"},method=RequestMethod.POST)
	private Map<String,List<ObjectNode>> fetchdata(@RequestBody final String body) throws IOException {
		
		clean();
		organization_id = get_org_name(body);		
		if(organization_id==null || organization_id=="") { //check null or empty value
			display.put("Organization id can't be null or empty,Please write Correct Organization id.",result);
			return display;
		}
		//System.out.println("----"+organization_id);
		
		final String GET_URL = "https://api.github.com/orgs/"+organization_id+"/repos";
		try {
			URL url = new URL(GET_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			
			System.out.println(responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				//System.out.println("-----");
				try (final Reader reader = new InputStreamReader(con.getInputStream())) {
		            inputLine = CharStreams.toString(reader);
		            
		            return processdata(inputLine);
		        }
			}
			else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
				//System.out.println("YES");
				display.put("Wrong Organization,Please write correct organization id.",result);
				return display;
			}
		}catch(Exception e) {
			System.out.println(e);
		}
		return display;
	}

	private void clean() {
		// TODO Auto-generated method stub
		
		display.clear();
		result.clear();
		
	}

	private String get_org_name(String body) throws IOException {
		// TODO Auto-generated method stub
		
		final JsonNode query = mapper.readTree(body);
		if(query.hasNonNull("org")) {
			return query.get("org").asText();
		}
		return null;
	}

	private Map<String,List<ObjectNode>> processdata(String input) throws IOException {
		// TODO Auto-generated method stub
		
		JsonNode array = (JsonNode)mapper.readTree(String.valueOf(input));
         
		ObjectNode fir= mapper.createObjectNode();
		fir.put("name", "null").put("stars", -1);
		ObjectNode sec= mapper.createObjectNode();
		sec.put("name", "null").put("stars", -1);
		ObjectNode third= mapper.createObjectNode();
		third.put("name", "null").put("stars", -1);
		
		for(JsonNode node:array) {
		    if(node.get("stargazers_count").asLong() > fir.get("stars").asLong()) {
		    	third=sec.deepCopy();
		    	sec=fir.deepCopy();
		    	fir.put("name", node.get("name").asText());
		    	fir.put("stars", node.get("stargazers_count").asLong());
		    }else if(node.get("stargazers_count").asLong() > sec.get("stars").asLong()) {
		    	third=sec.deepCopy();
		    	sec.put("name", node.get("name").asText());
		    	sec.put("stars", node.get("stargazers_count").asLong());
		    }else if(node.get("stargazers_count").asLong() > third.get("stars").asLong()){
		    	third.put("name", node.get("name").asText());
		    	third.put("stars", node.get("stargazers_count").asLong());
		    }
		}
		if(array.size()==0) { //check for no repo
			display.put("No Repository present.", result);
			return display;
		}else if(array.size()==1) { //check for 1 repo
			result.add(fir);
		}else if(array.size()==2) { //check for 2 repo
			result.add(fir);
			result.add(sec);
		}else {
			result.add(fir);
			result.add(sec);
			result.add(third);
		}
		display.put("results", result);
		return display;
		
	}
	
	 
}