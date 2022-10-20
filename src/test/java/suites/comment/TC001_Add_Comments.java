package suites.comment;

import api.IssueAPI;
import base.TestBase;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import reportUtils.Assertion;
import reportUtils.LogReport;
import utils.RestAssuredConfiguration;

import java.util.Hashtable;

public class TC001_Add_Comments extends TestBase {
    private IssueAPI issueAPI = new IssueAPI();
    private Assertion assertion = new Assertion();
    private LogReport logReport = new LogReport();
    private RestAssuredConfiguration restAssuredConfiguration = new RestAssuredConfiguration();

    @Test(dataProvider = "getDataForTest", description = "Verify That User Can Add Comment into Issue")
    public void TC001(Hashtable<String, Object> data) {
        int statusCode = ((Double) data.get("statusCode")).intValue();

        logReport.logStep("Step #1: Create An Issue successfully");
        String accessToken = restAssuredConfiguration.getAuth2Token();
        JSONObject payload = utils.JsonConverter.toJsonObject(data.get("PayLoadCreateIssue"));
        Response response = issueAPI.createIssue(accessToken, payload);
        JSONObject responseBody = new JSONObject(response.body().asString());
        String issueId = responseBody.getString("id");

        stepDescription = "Step #2: Add Comment into Issue";
        JSONObject payloadAddComment = utils.JsonConverter.toJsonObject(data.get("PayLoadAddComment"));
        response = issueAPI.addComment(accessToken, issueId, payloadAddComment);

        stepDescription = "Step #3: Verify Status Code: " + statusCode;
        assertion.assertEquals(response.statusCode(), statusCode, stepDescription);

        stepDescription = "Step #4: Verify Response Contains Comment ID";
        responseBody = new JSONObject(response.body().asString());
        String commentPostId = responseBody.getString("id");
        assertion.assertNotNull(commentPostId, stepDescription);

        stepDescription = "Step #5.1: Get Comment In Issue";
        response = issueAPI.getComment(accessToken, issueId);
        responseBody = new JSONObject(response.body().asString());
        JSONArray comment = responseBody.getJSONArray("comments");
        JSONObject commentObject = (JSONObject) comment.get(0);

        stepDescription = "Step #5.2: Verify Comment ID Matching";
        String commentGetId = commentObject.getString("id");
        assertion.assertEquals(commentPostId, commentGetId, stepDescription);
        JSONObject commentBody = commentObject.getJSONObject("body");

        stepDescription = "Step #5.3: Verify Comment Text Matching";
        JSONArray content = commentBody.getJSONArray("content");
        JSONObject contentActual = content.getJSONObject(0);
        String contentActualText = contentActual.getJSONArray("content").getJSONObject(0).getString("text");
        JSONObject contentExpectedObject = (JSONObject) payloadAddComment.get("body");
        JSONObject contentExpectedObject2 = (JSONObject) contentExpectedObject.getJSONArray("content").get(0);
        JSONArray contentExpectedArray = contentExpectedObject2.getJSONArray("content");
        JSONObject contentExpectedText = (JSONObject) contentExpectedArray.get(0);
        String contentExpected = contentExpectedText.getString("text");
        assertion.assertEquals(contentActualText, contentExpected, stepDescription);
    }

    @Test(dataProvider = "getDataForTest", description = "Verify That User Cannot Add Comment with Empty Text")
    public void TC002(Hashtable<String, Object> data) {
        int statusCode = ((Double) data.get("BadRequestStatus")).intValue();

        logReport.logStep("Step #1: Create issue");
        String accessToken = restAssuredConfiguration.getAuth2Token();
        JSONObject payload = utils.JsonConverter.toJsonObject(data.get("PayLoadCreateIssue"));
        Response response = issueAPI.createIssue(accessToken, payload);
        JSONObject responseBody = new JSONObject(response.body().asString());
        String issueId = responseBody.getString("id");

        stepDescription = "Step #2: Add Comment Into Issue with Empty Text";
        JSONObject payloadAddComment = utils.JsonConverter.toJsonObject(data.get("PayLoadAddCommentEmptyText"));
        Response AddCommentResponse = issueAPI.addComment(accessToken, issueId, payloadAddComment);

        stepDescription = "Step #3: Verify status code return to" + statusCode;
        assertion.assertEquals(AddCommentResponse.statusCode(), statusCode, stepDescription);

        stepDescription = "Step #4: Verify error comment response ";
        responseBody = new JSONObject(AddCommentResponse.body().asString());
        JSONObject errors = responseBody.getJSONObject("errors");
        String errorSummary = errors.getString("comment");
        System.out.println(errorSummary);
        assertion.assertEquals(errorSummary, (String) data.get("ErrorsComment"), "Errors Comment display");
    }
}
