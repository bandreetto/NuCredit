#!/usr/bin/env bash

lein test src/nucredit/tests/service_tests.clj
lein test src/nucredit/tests/ledger_tests.clj