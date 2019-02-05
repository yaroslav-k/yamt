# yamt

## Yet Another Money Tracker

So. What am I building here?
This is going to be expense/income/invest portfolio tracker with budgeting, regular bills and so on.
Now I'm still using Microsoft Money Plus Deluxe (yes, I'm serious). It's really good but oh man, it's time to create something new.

This will (hopefully) be something modern. Tech stack that will be used:
- Spring Boot server with microservices
- Hosted on Amazon AWS
- Using Amazon RDS (not sure yet)
- Admin interface implemented via Spring MVC (small part) and Vaadin Framework (most part)
- Web Applications - ReactJS-based mobile-friendly PWA with ability to work offline and synchronize in the background
- (Possibly. Not sure yet) React Native mobile application.

## Functional Requirements
1. Maximize users' anonimity. Only e-mail needed (possibly use of blockchain authorization). Though, authorization via OAuth2 providers like Google or Facebook is also possible.
1. Multi-currency support (including crypto currencies)
1. Accounts
1. Investment portfolio support (deposits, stocks, etc.)
1. Real-time stocks, currencies rates
1. Regular bills
1. Budget planning
1. Internationalization support
1. Lots of reports
1. Receipts scan with automatic registering (QR codes are used in Russia, not sure about other countries)
1. Enter spendings in offline from mobile
1. TBD

