#!/bin/bash
sbt fullOptJS
cp js/target/scala-2.12/lambda-opt.js .
