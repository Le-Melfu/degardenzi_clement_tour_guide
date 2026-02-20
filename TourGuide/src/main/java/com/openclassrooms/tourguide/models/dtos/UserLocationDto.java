package com.openclassrooms.tourguide.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationDto {

	private double latitude;
	private double longitude;
}
