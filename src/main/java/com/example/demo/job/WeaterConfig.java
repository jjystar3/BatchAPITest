package com.example.demo.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.entity.ApiResult;
import com.example.demo.repository.ApiResultRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WeaterConfig {

	String serviceKey = "";
	String dataType = "JSON";
	String code = "11B20201";	
	String ExecutionContext;
	
	@Autowired
	ApiResultRepository apiResultRepository;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	PlatformTransactionManager manager;
	
	@Bean
	public Job apiJob() throws IOException{
		return new JobBuilder("api job", jobRepository).start(step1()).next(step2()).next(step3()).build();
	}
	
	@Bean
	public Step step1() throws IOException{
		return new StepBuilder("Step1. API 호출하기", jobRepository)
				.tasklet(test1Tasklet(), manager).build();
	}
	
	@Bean
	public Step step2() throws IOException{
		return new StepBuilder("Step2. 응답 데이터 파싱하기", jobRepository)
				.tasklet(test2Tasklet(), manager).build();
	}

	@Bean
	public Step step3() throws IOException{
		return new StepBuilder("Step3. API 호출 결과를 테이블에 저장하기", jobRepository)
				.tasklet(test3Tasklet(), manager).build();
	}
	
	@Bean
	public Tasklet test1Tasklet() throws IOException{//예외처리
		return((contribution, chunkContext)->{
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
	        
	        ExecutionContext = sb.toString();
			return RepeatStatus.FINISHED;
		});
	}
	
	@Bean
	public Tasklet test2Tasklet() throws IOException{//예외처리
		return((contribution, chunkContext)->{
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			Root root = null;
			root = mapper.readValue(ExecutionContext, Root.class);
			System.out.println(root.response.header.resultCode);
			System.out.println(root.response.header.resultMsg);
			System.out.println(root.response.body.totalCount);
			return RepeatStatus.FINISHED;
		});
	}
	
	@Bean
	public Tasklet test3Tasklet() throws IOException{//예외처리
		return((contribution, chunkContext)->{
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			String weather = ExecutionContext;
			Root root = null;
			root = mapper.readValue(weather, Root.class);

			ApiResult apiResult = ApiResult.builder()
										.resultCode(root.response.header.resultCode)
										.resultMsg(root.response.header.resultMsg)
										.totalCount(root.response.body.totalCount)
										.build();
			
			apiResultRepository.save(apiResult);
			
			return RepeatStatus.FINISHED;
		});
	}
}
