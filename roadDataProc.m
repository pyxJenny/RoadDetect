clear;
clc;
[rms, rmsTime] = readRms;
[lat, lng, addTime] = readAddress;
[rmsNew] = removeRms(addTime, rms, rmsTime);
subplot(2,1,1);
plot(lat,lng);
subplot(2,1,2);
plot(rmsNew);
smoothRms = sgolayfilt(rmsNew,7,21);
figure;
plot(1:length(rmsNew),smoothRms);grid on
axis tight;