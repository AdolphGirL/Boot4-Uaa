package com.kd.ci.interfaces.controller.map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MapController {
	
	@GetMapping("/map/tgos")
	public String showTgosMap(Model model) {
		return "map/map-tgos";
	}
	
	@GetMapping("/map/tgos-multi")
	public String showTgosMultiMap(Model model) {
		return "map/map-tgos-multi";
	}
}
