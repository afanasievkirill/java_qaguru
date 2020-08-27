package tests.qa_automation.github;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openqa.selenium.By;
import qa_automation.github.model.Issue;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;
import static io.qameta.allure.Allure.parameter;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;


@Feature("Создание задачи")
@Story("Использование листнеров")
@Owner("afanasievkirill")
public class CreateIssueWithListnerTests extends BaseTest {

    @DisplayName("Создание задачи и проверка через АПИ с использованием Листнеров")
    @ParameterizedTest
    @CsvFileSource(resources = "/githubProperties.csv", numLinesToSkip = 1)
    public void createIssueWithListner(String LOGIN, String PASSWORD, String REPOSITORY, String TOKEN) {
        parameter("Репозиторий", REPOSITORY);
        parameter("Пользователь", LOGIN);
        
        open("https://github.com/");
        
        $x("//*[@class='HeaderMenu-link no-underline mr-3']").click(); // maybe a better locator?
        $(byName("login")).setValue(LOGIN);
        $(byName("password")).setValue(PASSWORD).pressEnter();
        
        // todo make assert that you are logged in
        
        $(".header-search-input").click();
        $(".header-search-input").sendKeys(REPOSITORY);
        $(".header-search-input").submit();
        $(By.linkText(REPOSITORY)).click();
        $(by("data-content", "Issues").click();
        $(".btn.btn-primary span").click();
        $("#issue_title").setValue(issueTitle);
        $("#issue_body").setValue(issueBody);
        $("#assignees-select-menu").click();
        // Нестабильный локатор
        $(".js-username").click();
        $("#assignees-select-menu").click();
        $(byText("Labels")).click();
        $(byText("bug")).click();
        $(byText("Labels")).click();
        $(byText("Submit new issue")).click();
          
        int issueNumber = Integer.parseInt($(".js-issue-title").sibling(0).getText().replace("#", ""));
        issue = given()
                .filter(new AllureRestAssured())
                .header("Authorization", "token " + TOKEN)
                .baseUri("https://api.github.com")
                .when()
                .get("repos/"+ REPOSITORY + "/issues/" + issueNumber)
                .then()
                .log()
                .all()
                .extract()
                .as(Issue.class);
        
        assertThat(issue.getNumber(), comparesEqualTo(issueNumber));
        assertThat(issue.getTitle(), equalTo(issueTitle));
        assertThat(issue.getBody(), equalTo(issueBody));
    }
}
