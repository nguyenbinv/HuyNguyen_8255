package suites.issues;

import api.IssueAPI;
import base.TestBase;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.Test;
import reportUtils.Assertion;
import reportUtils.LogReport;
import utils.RestAssuredConfiguration;

import java.util.Hashtable;

public class TC001_Create_Issue extends TestBase {
    private IssueAPI issueAPI = new IssueAPI();
    private Assertion assertion = new Assertion();
    private LogReport logReport = new LogReport();
    private RestAssuredConfiguration restAssuredConfiguration = new RestAssuredConfiguration();

    @Test(dataProvider = "getDataForTest", description = "Verify That User Can Create Issue Successfully With Valid Data")
    public void TC001(Hashtable<String, Object> data) {
        int statusCode = ((Double) data.get("SuccessStatusCode")).intValue();

        logReport.logStep("Step #1: Get Access Token");
        String accessToken = restAssuredConfiguration.getAuth2Token();

        logReport.logStep("Step #2: Call Create Issue API");
        JSONObject payload = utils.JsonConverter.toJsonObject(data.get("body"));
        Response response = issueAPI.createIssue(accessToken, payload);

        stepDescription = "Step #3: Verify Status Code: " + statusCode;
        assertion.assertEquals(response.statusCode(), statusCode, stepDescription);

        stepDescription = "Step #4: Verify Response Contains Key and Issued ID";
        JSONObject responseBody = new JSONObject(response.body().asString());
        String issueKey = responseBody.getString("key");
        String issueId = responseBody.getString("id");
        assertion.assertNotNull(issueKey, stepDescription);
        assertion.assertNotNull(issueId, stepDescription);

        stepDescription = "Step #5: Verify Issue Create Successfully";
        response = issueAPI.getIssue(accessToken, issueKey);
        assertion.assertEquals(response.statusCode(), "200", stepDescription);
        responseBody = new JSONObject(response.body().asString());
        assertion.assertEquals(responseBody.getString("key"), issueKey, "Issue's Key is matched");
        assertion.assertEquals(responseBody.getString("id"), issueId, "Issue's ID is matched");
    }

    @Test(dataProvider = "getDataForTest", description = "Verify That User Cannot Create Issue With Empty Summary")
    public void TC002(Hashtable<String, Object> data) {
        int statusCode = ((Double) data.get("BadRequestStatus")).intValue();

        logReport.logStep("Step #1: Get Access Token");
        String accessToken = restAssuredConfiguration.getAuth2Token();

        logReport.logStep("Step #2: Call Create Issue API");
        JSONObject payload = utils.JsonConverter.toJsonObject(data.get("bodyEmptySummary"));
        Response response = issueAPI.createIssue(accessToken, payload);

        stepDescription = "Step #3: Verify status code: " + statusCode;
        assertion.assertEquals(response.statusCode(), statusCode, stepDescription);

        stepDescription = "Step #4: Verify Error Summary Response";
        JSONObject responseBody = new JSONObject(response.body().asString());
        JSONObject errors = responseBody.getJSONObject("errors");
        String errorSummary = errors.getString("summary");
        System.out.println(errorSummary);
        assertion.assertEquals(errorSummary, (String) data.get("ErrorsSummary"), "Error Summary Response displays");
    }
}
