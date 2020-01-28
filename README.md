# Paper: Accessibility Issues in Android Apps: State of Affairs, Sentiments, and Ways Forward #

This repository contains:
* The automated accessibility evaluation framework, which assesses Android apps for their accessibility.
* The apps that were used in the study.
* The survey that was sent to the developers.

The DOI for this repository is [![DOI]()]()

## 1. Accessibility Evaluation Framework ##
To evaluate the accessibility features of Android apps, we developed an accessibility evaluation tool that leverages the accessibility checks provided by Google's Accessibility Testing Framework, which is an open-source library that supports various accessibility-related checks and can be applied to various UI elements such as TextView, ImageView, and Button.
Our accessibility evaluation tool has two major parts. One part simulates user interactions and the other part monitors the device for Accessibility Events.

* Running the App and simulating User Interactions
We assess the accessibility of apps dynamically, as several UI elements in Android are populated at runtime. To that end, our tool first installs the app on an emulator, then the tool uses Android Monkey to simulate user interaction. It runs each app for a time limit of 30 minutes, during which the app is restarted multiple times to maximize the coverage of Activities and prevent Monkey from getting stuck on specific screens.
In the case of a crash, the tool restarts the app and continues to crawl. Monkey takes a value as the seed to generate random events. We feed Monkey with a different seed value for each run to maximize coverage. Additionally, at this step, we collect coverage metrics, such as the number of covered Activities and lines of code. 

*The install.md file explains how the framework can be run to evalaute apps.*

## 2. Apps Data ##
The `Results` directory contains:
- List of apps used in the study, the APKs were obtained from https://androzoo.uni.lu/

## 3. Survey data ##
The `Survey` contains the survey that was sent to the developers to get their feedback.



