package com.example.demo.openapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.entity.ApiResult;
import com.example.demo.repository.ApiResultRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class OpenAPITest {

	@Autowired
	ApiResultRepository repository;
	
	String serviceKey = "5FLwWX19bZ8QF%2BzjaCAtXOjAnwu8Ozh8aRsfrOXL0%2B6XHnVB%2Bis9P8qJTjqicRSMxVyHq%2Fal8lxwHWPAbfHFkg%3D%3D";
	String dataType = "JSON";
	String code = "11B20201";	
	
	public String getWeather() throws IOException {
		StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstMsgService/getLandFcst"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + serviceKey); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode(dataType, "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        urlBuilder.append("&" + URLEncoder.encode("regId","UTF-8") + "=" + URLEncoder.encode(code, "UTF-8")); /*11A00101(백령도), 11B10101 (서울), 11B20201(인천) 등... 별첨 엑셀자료 참조(‘육상’ 구분 값 참고)*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        System.out.println(sb.toString());
        
		return sb.toString();
	}

	@Test
	public void jsonToDto() throws IOException {

		ObjectMapper mapper = new ObjectMapper();

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String weather = getWeather();

		Root root = null;

		root = mapper.readValue(weather, Root.class);

		System.out.println("결과코드: " + root.response.header.resultCode);

		System.out.println("결과메세지: " + root.response.header.resultMsg);
		
		System.out.println("전체결과수: " + root.response.body.totalCount);
	
	}
	

	@Test
	public void 데이터추가() throws IOException {

		ObjectMapper mapper = new ObjectMapper();

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String weather = getWeather();

		Root root = null;

		root = mapper.readValue(weather, Root.class);

		ApiResult apiResult = ApiResult.builder()
									.resultCode(root.response.header.resultCode)
									.resultMsg(root.response.header.resultMsg)
									.totalCount(root.response.body.totalCount)
									.build();
		
		repository.save(apiResult);
	}
	
	@Test
	public void 데이터조회() {
		List<ApiResult> list = repository.findAll();
		for(ApiResult apiResult : list) {
			System.out.println(apiResult);
		}
	}

	@Test
	public void 데이터단건조회() {
		Optional<ApiResult> result = repository.findById(1);
		if(result.isPresent()) {
			ApiResult apiResult = result.get();
			System.out.println(apiResult);
		}
	}

	@Test
	public void 데이터수정() throws IOException {

		ObjectMapper mapper = new ObjectMapper();

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String weather = getWeather();

		Root root = null;

		root = mapper.readValue(weather, Root.class);

		Optional<ApiResult> result = repository.findById(1);
		if(result.isPresent()) {
			ApiResult apiResult = result.get();
			// 내용 변경
			apiResult.setResultCode(root.response.header.resultCode);
			apiResult.setResultMsg(root.response.header.resultMsg);
			apiResult.setTotalCount(root.response.body.totalCount);
			// 데이터 업데이트
			repository.save(apiResult);
		}
	}
	
	@Test
	public void 데이터삭제() {
		repository.deleteById(1);
	}

}
