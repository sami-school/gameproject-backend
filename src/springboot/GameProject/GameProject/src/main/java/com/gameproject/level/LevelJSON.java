package com.gameproject.level;


// this is used to store Json object for creating level because we need the session id for user 
public class LevelJSON {
	
		private String session;
		private String data;
		private String name;
		

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setSession(String session) {
			this.session = session;
		}

		public String getSession() {
			return session;
		}

		

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}


	


}
