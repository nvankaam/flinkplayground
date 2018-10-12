package org.codefeedr.experiments
import com.typesafe.scalalogging.LazyLogging
import org.codefeedr.core.library.internal.LoggingSinkFunction
import org.codefeedr.experiments.model.{HotIssue, HotPr, IssueCommentPr}

object HotPullrequestStandalone {

  def main(args: Array[String]): Unit = {
    val query = new HotPullrequestStandalone()
    query.deploy(args)
  }

}

class HotPullrequestStandalone extends HotPullRequestQueryBase {

  def deploy(args: Array[String]): Unit = {

    logger.info("Initializing arguments")
    initialize(args)
    logger.info("Arguments initialized")
    val env = getEnvironment

    val issueComments = getIssueComments()
    val issues = getIssues()
    val pullRequestComments = getPullRequestComments()

    //val issuePrs = getIssueCommentPullrequest(issues,issueComments)
    val hotIssue = getHotIssues(issueComments)
    val hotIssuePrs = getHotIssuePullRequests(hotIssue, issues)

    val hotPrs = getHotPullRequests(pullRequestComments)

    val merged = mergeHotPullRequest(hotIssuePrs, hotPrs)

    //val sink = new LoggingSinkFunction[HotPr]("HotPrSink")
    val sink = new LoggingSinkFunction[HotPr]("IssueSink")
    //issues.addSink(o => Console.println(o))
    //hotIssuePrs.addSink(sink)
    merged.addSink(sink)
    //merged.addSink(o => Console.println(o))
    //logger.info("Submitting hot issue query job")
    env.execute("HotIssues")
  }

}
