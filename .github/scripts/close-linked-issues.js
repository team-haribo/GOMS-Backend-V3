'use strict';

const ISSUE_SECTION_HEADING = /^##[ \t]*(?:🔗[ \t]*)?관련 이슈[ \t]*$/u;
const NEXT_SECTION_HEADING = /^##[ \t]+/u;
const RESOLVING_ISSUE_LINE = /^\s*[-*]\s*(?:closes|fixes|resolves)\s*:?\s+#(\d+)\b/i;

function extractResolvingIssueNumbers(body = '') {
  const lines = String(body).split(/\r?\n/);
  const sectionStart = lines.findIndex((line) => ISSUE_SECTION_HEADING.test(line));

  if (sectionStart === -1) {
    return [];
  }

  const nextSection = lines.findIndex(
    (line, index) => index > sectionStart && NEXT_SECTION_HEADING.test(line),
  );
  const sectionEnd = nextSection === -1 ? lines.length : nextSection;
  const issueNumbers = [];

  for (const line of lines.slice(sectionStart + 1, sectionEnd)) {
    const match = RESOLVING_ISSUE_LINE.exec(line);
    if (match) {
      issueNumbers.push(Number(match[1]));
    }
  }

  return [...new Set(issueNumbers)];
}

async function closeLinkedIssues({ github, context, core }) {
  const issueNumbers = extractResolvingIssueNumbers(context.payload.pull_request?.body ?? '');

  if (issueNumbers.length === 0) {
    core.info('No resolving issues found in the related issue section.');
    return;
  }

  const { owner, repo } = context.repo;

  for (const issueNumber of issueNumbers) {
    try {
      const { data: issue } = await github.rest.issues.get({
        owner,
        repo,
        issue_number: issueNumber,
      });

      if (issue.pull_request) {
        core.warning(`Skipping #${issueNumber}: the referenced item is a pull request.`);
        continue;
      }

      if (issue.state === 'closed') {
        core.info(`Skipping #${issueNumber}: already closed.`);
        continue;
      }

      await github.rest.issues.update({
        owner,
        repo,
        issue_number: issueNumber,
        state: 'closed',
        state_reason: 'completed',
      });
      core.info(`Closed issue #${issueNumber}.`);
    } catch (error) {
      core.warning(`Could not close issue #${issueNumber}: ${error.status ?? ''} ${error.message}`.trim());
    }
  }
}

module.exports = { closeLinkedIssues, extractResolvingIssueNumbers };
