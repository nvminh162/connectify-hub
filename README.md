# Handlebook

![Roadmap](./Spring%20boot%203%20road%20map.png)
![Roadmap](./Spring%20boot%203%20Microservices%20road%20map.png)

### Spring cloud
+ `Api gateway, Load balencer`: giống layer đứng on top của tất cả các microservice dưới nó, chịu trách nhiệm định tuyến request, cân bằng tải giữa các microservice với nhau
+ `Http clients (OpenFeign, ...)`: giúp các microservice giao tiếp với nhau thông qua các API RESTful
+ `Distributed logging, tracing, metric`: logging là log ra thông tin của hệ thống, tracing là theo dõi các request đi qua các microservice khác nhau, metric là đo lường hiệu suất của hệ thống
    - OpenTelemetry: một framework để thu thập dữ liệu giám sát và theo dõi hiệu suất của các ứng dụng phân tán
    - Prometheus: một hệ thống giám sát và cảnh báo mã nguồn mở, thường được sử dụng để thu thập và lưu trữ các số liệu hiệu suất của ứng dụng (metrics)
    - Grafana: một nền tảng phân tích và giám sát mã nguồn mở, thường được sử dụng để trực quan hóa dữ liệu từ Prometheus và các nguồn dữ liệu khác
    - Zipkin: một hệ thống theo dõi phân tán mã nguồn mở, giúp thu thập và hiển thị dữ liệu theo dõi từ các ứng dụng phân tán (Zipkin > tracing)
    - ELK stack
+ `Resilient microservices`: xây dựng các microservice có khả năng chịu lỗi và phục hồi nhanh chóng khi gặp sự cố
    - SAGA pattern: một mẫu thiết kế để quản lý các giao dịch phân tán trong kiến trúc microservices, bằng cách chia nhỏ các giao dịch lớn thành các bước nhỏ hơn và sử dụng các hành động bù đắp để đảm bảo tính nhất quán dữ liệu
    - Circuit Breaker pattern: một mẫu thiết kế để ngăn chặn sự cố lan
    - Bulkhead: một mẫu thiết kế để cô lập các phần của hệ thống nhằm ngăn chặn sự cố lan rộng
+ `Documenting with Swagger/OpenAPI`: tạo tài liệu API tự động cho các microservice, giúp các nhà phát triển dễ dàng hiểu và sử dụng các API

 ### Architecture
+ `DDD (Domain-Driven Design)`: một phương pháp thiết kế phần mềm tập trung vào việc hiểu và mô hình hóa miền nghiệp vụ (domain) của ứng dụng <thiết kế hệ thống theo Domain (Business), trong microservice chia nhỏ chịu trách nhiệm cho mỗi service/>
+ `Event Driven Architecture`: một kiến trúc phần mềm trong đó các thành phần giao tiếp với nhau thông qua việc phát và xử lý các sự kiện <hệ thống hoạt động theo event, khi có việc xảy ra event trickger các event không/>
    - Kafka
    - RabbitMQ

### DevOps
+ `Docker`: một nền tảng mã nguồn mở để tự động hóa việc triển khai, mở rộng và quản lý các ứng dụng trong các container
+ `Kubernetes`: một hệ thống mã nguồn mở để tự động hóa việc triển khai, mở rộng và quản lý các ứng dụng containerized
+ `AWS/Azure/GCP`: các nhà cung cấp dịch vụ đám mây lớn, cung cấp các dịch vụ hạ tầng và nền tảng để triển khai và quản lý các ứng dụng trên đám mây
+ `Github Actions, Gitlab CI/CD`: các công cụ tích hợp liên tục và triển khai liên tục (CI/CD) để tự động hóa quá trình xây dựng, kiểm thử và triển khai ứng dụng
+ `Jenkin`: một công cụ mã nguồn mở để tự động hóa quá trình xây dựng, kiểm thử và triển khai ứng dụng