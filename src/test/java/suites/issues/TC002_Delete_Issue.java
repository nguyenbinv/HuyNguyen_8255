package suites.issues;

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

public class TC002_Delete_Issue extends TestBase {
    private IssueAPI issueAPI = new IssueAPI();
    private Assertion assertion = new Assertion();
    private LogReport logReport = new LogReport();
    private RestAssuredConfiguration restAssuredConfiguration = new RestAssuredConfiguration();

    @Test(dataProvider = "getDataForTest", description = "Verify That User Can Delete Issue Successfully")
    public void TC001(Hashtable<String, Object> data) {
        int statusCode = ((Double) data.get("SuccessStatusCode")).intValue();

        logReport.logStep("Step #1: Get Access Token");
        String accessToken = restAssuredConfiguration.getAuth2Token();

        logReport.logStep("Step #2: Create Issue");
        JSONObject payload = utils.JsonConverter.toJsonObject(data.get("PayLoadCreateIssue"));
        Response response = issueAPI.createIssue(accessToken, payload);

        stepDescription = "Step #3: Verify Status Code: " + statusCode;
        assertion.assertEquals(response.statusCode(), statusCode, stepDescription);

        stepDescription = "Step #4: Verify Response Contains Key and Issue ID";
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

        stepDescription = "Step #6: Delete Issue";
        response = issueAPI.deleteIssue(accessToken, issueKey);
        assertion.assertEquals(response.statusCode(), "204", stepDescription);

        stepDescription = "Step #7: Verify That Issue is Deleted and Cannot get Deleted Issue";
        response = issueAPI.getIssue(accessToken, issueKey);
        assertion.assertEquals(response.statusCode(), "404", stepDescription);

        stepDescription = "Step #8: Verify Error Message Response ";
        responseBody = new JSONObject(response.body().asString());
        JSONArray errors = responseBody.getJSONArray("errorMessages");
        String errorMessage = errors.getString(0);
        System.out.println(errorMessage);
        assertion.assertEquals(errorMessage, (String) data.get("ErrorsSummary"), "Errors Summary displays");
    }
}
