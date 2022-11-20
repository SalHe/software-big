FROM python:3.7 AS py
FROM openjdk:11

# COPY *gradle* ./
# COPY gradle ./gradle
# RUN ./gradlew build
# RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys '3B4FE6ACC0B21F32'
# RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys '871920D1991BC93C'
# RUN echo "deb https://mirrors.tuna.tsinghua.edu.cn/debian/ bullseye main contrib non-free" >> /etc/apt/sources.list && \
#     echo "deb https://mirrors.tuna.tsinghua.edu.cn/debian/ bullseye-updates main contrib non-free" > /etc/apt/sources.list && \
#     echo "deb https://mirrors.tuna.tsinghua.edu.cn/debian/ bullseye-backports main contrib non-free" > /etc/apt/sources.list && \
#     echo "deb https://mirrors.tuna.tsinghua.edu.cn/debian-security bullseye-security main contrib non-free" >> /etc/apt/sources.list

# RUN apt-get update && \
#     apt-get install -y software-properties-common && \
    # apt-get install -y python3-pip

COPY --from=py / /

COPY ./requirements.txt ./
RUN pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple 


COPY . .
RUN ./gradlew bootJar

ENTRYPOINT [ "java", "-jar", "./build/libs/ExamManager-1.0.0.jar" ]