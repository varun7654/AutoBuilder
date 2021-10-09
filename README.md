# AutoBuilder
A gui tool that makes desigining and testing autos easier

![image](https://user-images.githubusercontent.com/59785640/136644756-be514ab1-5583-4ae3-9109-d63b7a29a373.png)

## Features
1. Interactive gui for designing robot autos.
2. Command based script items to allow you to control other parts of the robot.
3. Iterate without deploying. This tool uses networktables to upload your autonomous to the robot so you can quickly iterate without slowing down.
4. See how long your auto will take before you deploy. We're using the same trajectory and path planner that your robot uses. What you see is what you will get!
5. Real-time feedback. The path that your robot drives will be visible in the gui. Use this too see where things are going wrong and quickly fix them.


# Building
Follow these intructions: https://libgdx.com/dev/setup/
(You can skip the generating a project step)

## Get Compiled builds
1. Click Actions at the top of the page or visit this link: https://github.com/FRC3476/AutoBuilder/actions
2. Find the latest (closest to the top) action and click on the bold title next to it
3. At the bottom, there should be a text that says Artifacts, with a size next to it
4. Click the bold text labeled "Artifacts" to download a zip containing the built jar
5. You should now be able to just double click the jar and run it!
