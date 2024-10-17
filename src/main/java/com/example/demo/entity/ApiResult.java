package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@EntityListeners(value = { AuditingEntityListener.class })
@Entity
@Table(name="tbl_api_result")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int no;
    
	@CreatedDate
	LocalDateTime apiCallTime;

    @Column(length = 10)
    String resultCode;

    @Column(length = 20)
    String resultMsg;

    @Column(length = 11, nullable = false)
    int totalCount;

}
