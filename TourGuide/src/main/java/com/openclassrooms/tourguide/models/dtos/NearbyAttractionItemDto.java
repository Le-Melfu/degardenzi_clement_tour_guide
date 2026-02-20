package com.openclassrooms.tourguide.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyAttractionItemDto {

	private String attractionName;
	private double attractionLatitude;
	private double attractionLongitude;
	private double distanceInMiles;
	private int rewardPoints;
}
