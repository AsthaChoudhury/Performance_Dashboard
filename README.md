### Performance_Dashboard
##**Asset Performance Dashboard API Development using FastAPI**##
#Introduction
This is the Asset Performance Analytics Dashboard which is a FastAPI application designed to provide comprehensive insights, track and analyze the performance of assets within an organization. It offers features for asset and performance_metrics management, analytics and user authentication.It also helps in reducing downtime and improving operational efficiency.

#Features
- **ASSETS MANAGEMENT** – CRUD operations
- **PERFORMANCE MANAGENT** – CRUD operations
- **ANALYTICS and AUTHENTICATION** -  Authentication and authorization using Json Web Tokens tokens. Only after authorization users are allowed to access protected routes.
- **PERFORMANCE METRICS** - Calculation of average downtime, uptime, maintenance costs and failure rates
- Retrieval of assets with high failure rates

#Set-up
1. **Clone the Repository**
2. **Install Dependencies:**
   	bash
  	 pip install -r requirements.txt
3. **Set up the Database:**
    Configure the MongoDB connection string in `config/db.py`
4. **Set Environment Variables:**
   Create a `.env` file in the root directory.
   add- SECRET_KEY=your_secret_key
5. **Run the Server**:
   Start the server by running ‘uvicorm index:app –reload’
   --reload is used so that every time changes are made it directly changes in the server so no need to refresh the server everytime after making changes

#Usage:
1. **Run the server:**
    ‘uvicorm index:app –reload’
2. **Interact with the API:**
    Use tools like cURL, Postman, or HTTP client to send requests to the API endpoints.
    Refer to the API documentation for details on available endpoints and request/response formats.

   **ASSTES:-**
   	**GET /assets/**: Retrieve all assets.
   	**POST /assets/**: Create a new asset.
   	**GET /assets/{asset_id}**: Retrieve an asset by ID.
   	**PUT /assets/{asset_id}**: Update an asset.
  	**DELETE /assets/{asset_id}**: Delete an asset.
   
   **PERFORMANCE METRICs:-**
	**GET /performance/**: Retrieve all performance metrics.
	**POST /performance/**: Create a new performance metric.
	**GET /performance/{performance_metric_id}**: Retrieve a performance metric by ID.
	**PUT /performance/{performance_metric_id}**: Update a performance metric.
	**DELETE /performance/{performance_metric_id}**: Delete a performance metric.

   **Authentication**
	**POST /auth/token**: Obtain an access token for authentication.
	**GET /auth/protected**: Protected route requiring authentication.

   **Anaytics**
	Calculate various performance metrics and analyze asset data

#CONTRIBUTING:-
	Contributions are very welcome! If you'd like to contribute to this project, please fork the repository and submit a pull request with your changes.
	Follow these steps:-
		1-	Fork the repository
		2-	Create a new branch for your feature or improvement
		3-	Make your changes and commit them
		4-	Push your changes to the branch
		5-	Create a new pull request and describe your changes in the detail

#License
	This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

 #Acknowledgements
	**FastAPI Documentation**
	**Pydantic Documentation**
	**MongoDB Documentation**

 

  





   



   
   
   

   

   




