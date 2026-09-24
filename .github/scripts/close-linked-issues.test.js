'use strict';

const assert = require('node:assert/strict');
const { closeLinkedIssues, extractResolvingIssueNumbers } = require('./close-linked-issues');

const cases = [
  {
    name: 'single resolving issue',
    body: '## 🔗 관련 이슈\n\n- Closes #10\n\n## 📃 작업내용\n',
    expected: [10],
  },
  {
    name: 'multiple resolving issues',
    body: '##  관련 이슈\n\n- Closes #10\n- Fixes #11\n- Resolves #12\n',
    expected: [10, 11, 12],
  },
  {
    name: 'related issue is not a resolving issue',
    body: '## 🔗 관련 이슈\n\n- Related to #10\n',
    expected: [],
  },
  {
    name: 'mentions in another section are ignored',
    body: '## 📃 작업내용\n\n- Closes #10\n',
    expected: [],
  },
  {
    name: 'parsing stops at the next section',
    body: '## 🔗 관련 이슈\n\n- Closes #10\n\n## 📃 작업내용\n\n- Closes #20\n',
    expected: [10],
  },
  {
    name: 'duplicate issues are processed once',
    body: '## 🔗 관련 이슈\n\n- Closes #10\n- Fixes #10\n',
    expected: [10],
  },
  {
    name: 'cross repository references are ignored',
    body: '## 🔗 관련 이슈\n\n- Closes owner/repository#10\n',
    expected: [],
  },
];

for (const testCase of cases) {
  assert.deepEqual(extractResolvingIssueNumbers(testCase.body), testCase.expected, testCase.name);
}

async function verifyIssueStateHandling() {
  const updates = [];
  const warnings = [];
  const github = {
    rest: {
      issues: {
        async get({ issue_number: issueNumber }) {
          if (issueNumber === 13) {
            const error = new Error('not found');
            error.status = 404;
            throw error;
          }
          if (issueNumber === 14) {
            return { data: { state: 'closed' } };
          }
          if (issueNumber === 15) {
            return { data: { state: 'open', pull_request: {} } };
          }
          return { data: { state: 'open' } };
        },
        async update(payload) {
          updates.push(payload);
        },
      },
    },
  };
  const context = {
    repo: { owner: 'team-haribo', repo: 'GOMS-Backend-V3' },
    payload: {
      pull_request: {
        body: '## 🔗 관련 이슈\n\n- Closes #13\n- Closes #14\n- Closes #15\n- Closes #16\n- Fixes #16\n',
      },
    },
  };
  const core = {
    info() {},
    warning(message) {
      warnings.push(message);
    },
  };

  await closeLinkedIssues({ github, context, core });

  assert.deepEqual(updates, [
    {
      owner: 'team-haribo',
      repo: 'GOMS-Backend-V3',
      issue_number: 16,
      state: 'closed',
      state_reason: 'completed',
    },
  ]);
  assert.equal(warnings.length, 2, 'missing issue and pull request references warn without updating state');
}

verifyIssueStateHandling().then(() => {
  console.log(`Passed ${cases.length} linked issue parser cases and issue state handling cases.`);
}).catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
