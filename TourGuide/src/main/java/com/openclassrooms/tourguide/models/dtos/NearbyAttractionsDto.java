package com.openclassrooms.tourguide.models.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyAttractionsDto {

	private UserLocationDto userLocation;
	private List<NearbyAttractionItemDto> nearbyAttractions;
}
