\# MyGate App — Firestore Database Structure



\## Collection 1: users

Stores information about all app users (residents, guards, admins)



| Field  | Type   | Description                          |

|--------|--------|--------------------------------------|

| userId | string | Matches Firebase Auth UID            |

| name   | string | Full name of user                    |

| email  | string | Email address                        |

| role   | string | "resident" or "guard" or "admin"     |

| phone  | string | Phone number                         |



\## Collection 2: visitors

Stores every visitor entry log



| Field       | Type      | Description                         |

|-------------|-----------|-------------------------------------|

| name        | string    | Visitor's full name                 |

| phone       | string    | Visitor's phone number              |

| approvedBy  | string    | userId of resident who approved     |

| status      | string    | "pending", "approved", or "denied"  |

| arrivalTime | timestamp | Date and time of arrival            |



\## Collection 3: staff

Stores daily staff attendance and assignment



| Field      | Type      | Description                        |

|------------|-----------|------------------------------------|

| name       | string    | Staff member's name                |

| role       | string    | "security", "housekeeping", etc.   |

| assignedTo | string    | Block or area assigned to          |

| entryTime  | timestamp | Time they entered the premises     |

| exitTime   | timestamp | Time they exited the premises      |



\## Collection 4: patrols

Stores guard patrol logs



| Field       | Type             | Description                        |

|-------------|------------------|------------------------------------|

| guardId     | string           | userId of the guard on patrol      |

| startTime   | timestamp        | When patrol started                |

| endTime     | timestamp        | When patrol ended                  |

| status      | string           | "active" or "completed"            |

| checkpoints | array of strings | List of locations checked          |



\## Security Rules Summary

\- Users can only read/write their own profile

\- Visitors, Staff, Patrols — any logged-in user can read and write

\- Nobody can delete visitor records

