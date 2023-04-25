# AWS-S3-Upload-Library
A library that makes upload to AWS S3 bucket fasteasy, without any issues with missing file extension.

Follow the steps below:

**Step 1:** Login to AWS management console and go to S3:
1. Create an s3 bucket
<img width="1343" alt="Screenshot 2023-04-25 at 15 01 07" src="https://user-images.githubusercontent.com/34043759/234301470-e79657c5-7a7b-4744-a906-5a672a048fc7.png">

**Step 2:** Create identity pool:
1. Search for cognito from the management console
2. Click on federated entities
3. Enable access to unauthenticated identities

<img width="1418" alt="Screenshot 2023-04-25 at 15 06 21" src="https://user-images.githubusercontent.com/34043759/234302381-d10ee910-e558-4402-8ffe-872c4f8f9b78.png">

Open federated entities to create a new identity pool

<img width="1483" alt="Screenshot 2023-04-25 at 15 09 13" src="https://user-images.githubusercontent.com/34043759/234304056-10a7f913-68da-41dc-bd34-ba392b0d3a57.png">

Click on create new identity pool

<img width="1425" alt="Screenshot 2023-04-25 at 15 13 27" src="https://user-images.githubusercontent.com/34043759/234304736-0f637e91-08db-4ce3-8176-dbcc41e4b79b.png">

Enter your App name for identity pool name and enable access to unauthenticated identities and click on create pool.

<img width="1728" alt="Screenshot 2023-04-25 at 15 15 58" src="https://user-images.githubusercontent.com/34043759/234305661-74924ba2-9a5b-4e20-a411-06dbd339f667.png">

Click on the Allow button to create two default roles associated with your identity pool: one for unauthenticated users and one for authenticated users

<img width="1728" alt="Screenshot 2023-04-25 at 15 19 55" src="https://user-images.githubusercontent.com/34043759/234306660-7ba28699-3eba-471b-9bde-5dab0f1869c2.png">

Your identity pool id will be shown to you in the code snippet copy and save for later.

<img width="1728" alt="Screenshot 2023-04-25 at 15 22 30" src="https://user-images.githubusercontent.com/34043759/234308074-7e011a3e-a634-4014-adc3-b3f71ed91019.png">

**Step 3:** Grant permission to S3 bucket:
1. Go to [AWS IAM CONSOLE](https://console.aws.amazon.com/iam/home).
2. Select Roles from the side menu.
3. Search for Cognito_appnameUnath_Role 
4. Attach AmazonS3FullAccess policy to role

<img width="1690" alt="Screenshot 2023-04-25 at 15 31 16" src="https://user-images.githubusercontent.com/34043759/234312862-9e0d09be-f267-48e2-bc2c-c17b4572dae4.png">

Search for Cognito_appnameUnath_Role replace app name with your Pool Id name

<img width="1690" alt="Screenshot 2023-04-25 at 15 31 54" src="https://user-images.githubusercontent.com/34043759/234312971-259ecd74-4bd1-4a12-8294-37522a9f47ed.png">

Select Unauth role and click on add permissions to reveal attach policy option, click on attach policy.

<img width="1380" alt="Screenshot 2023-04-25 at 15 33 48" src="https://user-images.githubusercontent.com/34043759/234313669-196ffa63-0503-4354-a0b2-201632fc5aa9.png">

Search for AmazonS3FullAccess

<img width="1684" alt="Screenshot 2023-04-25 at 15 36 44" src="https://user-images.githubusercontent.com/34043759/234314180-a2d7cb91-945a-45f7-98d2-7456e6249808.png">

Attach policy or permission   

<img width="1684" alt="Screenshot 2023-04-25 at 15 39 26" src="https://user-images.githubusercontent.com/34043759/234314285-db8fb916-05b4-4385-adce-cd4d5a71f1bb.png">

**Step 4:** Add the following dependencies to Android studio

1. Add jitpack.io to your settings.gradle file

```pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' } // add it here
    }
}
````

2. Add the library dependency to app Gradle file.

```
implementation 'com.github.mavinomichael:AWS-S3-Upload-Library:1.0.1'
```
**Step 5:** Initialise S3Uploader object with the following details:

1. Copy the cognito pool Id and region frommthe code snippet provided when creating the cognito pool which we saved earlier.

```
// Initialize the Amazon Cognito credentials provider
CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
    getApplicationContext(),
    "identity-pool-id", // copy Identity pool ID
    Regions.US_EAST_2 // copy Region
);
```
2. Get your S3 bucket region by navigating to S3 click on buckets, select your bucket and click on properties.

<img width="1378" alt="Screenshot 2023-04-25 at 15 59 31" src="https://user-images.githubusercontent.com/34043759/234319164-9b88e968-0a1c-4ea6-8a55-56c87667e73a.png">

(from the screenshot above the region for this bucket will be Regions.US_EAST_2).

3. Copy the bucket name 

4. Initialise S3Uploader object like this:

Create a Kotlin object and add all the details copied above like this

```
object AWSKeys {
    const val COGNITO_POOL_ID = "cognito-pool-id"
    const val BUCKET_NAME = "bucket-name"
    val COGNITO_REGION = Regions.US_EAST_2
    val BUCKET_REGION = Regions.US_EAST_2
}

```

Reference the keys when creating the S3UploaderObject and call the uploadFile function.

```
val s3uploaderObj = S3Uploader(
            context,
            AWSKeys.BUCKET_NAME,
            AWSKeys.COGNITO_POOL_ID,
            AWSKeys.COGNITO_REGION,
            AWSKeys.BUCKET_REGION
        )
        
 //upload file to S3       
 fun uploadVideoTos3(videoUri: Uri, context: Context): String {
 
        //upload file with s3UploaderObject
        val fileUrl = s3uploaderObj.uploadFile(
            videoUri.path!!,
            "video/mp4",
            true
        )
        return fileUrl
     }

```
The uploadFile function returns the file URL from S3

5. Set a listener for events during upload, like progress of upload, success, error, upload id, and state of upload.

```
s3uploaderObj.setOnUploadListener(object : S3UploadListener {
            override fun onSuccess(id: Int, response: String?) {
            
                Log.i(TAG, "viewModel status $response for uploadId: $id")
                
                if (response.equals("success", ignoreCase = true)) {
                   Log.i(TAG, "upload was successful with response: $response for uploadId: $id" 
                } else {
                    Log.i(TAG, "upload failed with response: $response")
                }
            }

            override fun onError(id: Int, response: String?) {
                Log.i(TAG, "viewModel error $response for uploadId: $id")
            }

            override fun onProgress(id: Int, progress: Int) {
                Log.i(TAG, "viewModel progress $progress for uploadId: $id")
            }

            override fun onStateChanged(
                id: Int,
                state: UploadState?
            ) {
                Log.d(TAG, "onStateChanged: $state")
            }

        })
```

Enjoy!
