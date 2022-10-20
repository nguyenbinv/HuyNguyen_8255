package suites.comment;

import api.IssueAPI;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import reportUtils.Assertion;
import reportUtils.LogReport;
import base.TestBase;
import utils.RestAssuredConfiguration;

import java.util.Hashtable;

public class TC002_Update_Comment extends TestBase {
    private IssueAPI issueAPI = new IssueAPI();
    private Assertion assertion = new Assertion();
    private LogReport logReport = new LogReport();
    private RestAssuredConfiguration restAssuredConfiguration = new RestAssuredConfiguration();

    @Test(dataProvider = "getDataForTest", description = "Verify That User Can Update Comment")
    public void TC001(Hashtable<String, Object> data) {
        int statusCode = ((Double) data.get("SuccessStatusCode")).intValue();

        logReport.logStep("Step #1: Create Issue");
        String accessToken = restAssuredConfiguration.getAuth2Token();
        JSONObject payload = utils.JsonConverter.toJsonObject(data.get("PayLoadCreateIssue"));
        Response response = issueAPI.createIssue(accessToken, payload);
        JSONObject responseBody = new JSONObject(response.body().asString());
        String issueId = responseBody.getString("id");

        stepDescription = "Step #2: Add a Comment Into Created Issue";
        JSONObject payloadAddComment = utils.JsonConverter.toJsonObject(data.get("PayLoadAddComment"));
        response = issueAPI.addComment(accessToken, issueId, payloadAddComment);
        responseBody = new JSONObject(response.body().asString());
        String commentId = responseBody.getString("id");

        stepDescription = "Step #3: Update Comment";
        JSONObject payloadUpdateComment = utils.JsonConverter.toJsonObject(data.get("PayLoadUpdateComment"));
        response = issueAPI.updateComment(accessToken, issueId, commentId, payloadUpdateComment);

        stepDescription = "Step #4: Verify Status Code: " + statusCode;
        assertion.assertEquals(response.statusCode(), statusCode, stepDescription);

        stepDescription = "Step #5: Verify Response ID Match with Comment ID";
        responseBody = new JSONObject(response.body().asString());
        assertion.assertEquals(responseBody.getString("id"), commentId, stepDescription);

        stepDescription = "Step #6: Verify Response Created Time and Updated Time are Different";
        assertion.assertNotEquals(responseBody.getString("created"), responseBody.getString("updated"), stepDescription);

        stepDescription = "Step #7: Verify Comment Text is Matched";
        JSONObject body = responseBody.getJSONObject("body");
        System.out.println("````````````````````````````");
        JSONArray content = body.getJSONArray("content");
        JSONObject contentObject = content.getJSONObject(0);
        JSONObject contentArray = contentObject.getJSONArray("content").getJSONObject(0);
        String contentActualText = contentArray.getString("text");
        System.out.println(contentActualText);

        JSONObject contentPayloadObject = (JSONObject) payloadUpdateComment.get("body");
        JSONObject contentPayloadObject2 = (JSONObject) contentPayloadObject.getJSONArray("content").get(0);
        JSONArray contentExpectedArray = contentPayloadObject2.getJSONArray("content");
        JSONObject contentExpectedText = (JSONObject) contentExpectedArray.get(0);
        String contentExpected = contentExpectedText.getString("text");

        assertion.assertEquals(contentActualText, contentExpected, stepDescription);

        stepDescription = "Step #8: Get Updated Comment";
        response = issueAPI.getCommentId(accessToken, issueId, commentId);

        stepDescription = "Step #9: Verify Updated Comment Is Matched with Expected";
        responseBody = new JSONObject(response.body().asString());
        body = responseBody.getJSONObject("body");
        content = body.getJSONArray("content");
        contentObject = content.getJSONObject(0);
        contentArray = contentObject.getJSONArray("content").getJSONObject(0);
        contentActualText = contentArray.getString("text");
        assertion.assertEquals(contentActualText, contentExpected, stepDescription);
    }
}
