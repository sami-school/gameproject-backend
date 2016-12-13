package com.gameproject.responses;

import com.gameproject.level.Level;

public class GetLevelResponse {

	private Level level;

	public GetLevelResponse(Level level) {
		this.level = level;
	}

	public Level getLevel() {
		return level;
	}

}
