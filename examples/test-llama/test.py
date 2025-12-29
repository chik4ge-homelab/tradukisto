from openai import OpenAI

endpoint = "http://localhost:8081/v1"

SYSTEM_PROMPT = """
<|plamo:op|>dataset
translation
"""

USER_PROMPT_FROM = """
<|plamo:op|>input lang=English
"""

USER_PROMPT_TO = """
<|plamo:op|>output lang=Japanese
"""

TARGET_TEXT = """
The main Sapporo campus is located just north of Sapporo Station, in the heart of Sapporo City. The entirety of the campus measures approximately 180 hectares and houses academic and administrative buildings, research laboratories, student dormitories, and farmland. The main academic buildings are found along a 1.5 kilometer stretch of road that runs from the Main Gate to the Kita 18 Gate, roughly encompassing the distance between Kita-Juni-Jo and Kita 18-jo subway stations on the Namboku Subway Line. A campus-wide bus service runs regular routes between the southern and northern end of the university, although access is restricted to university staff only.[18]

The abundance of accessible green space has continued to be popular not only among students, but also the general public, who can often be seen using the campus area in a similar way to a public park. Walking tours of the campus for interested foreign and domestic tourists are provided by several businesses in Sapporo, although no tour is needed to visit the campus.[19] Fall is an especially popular time for campus visits, with tourists and Sapporo residents flocking to get a view of the golden ginkgo trees that line Ginkgo Avenue.[20] 
"""

client = OpenAI(
    base_url=endpoint,
    api_key="hogehoge",
)

response = client.chat.completions.create(
    model="plamo2-translate",
    temperature=0,
    messages=[
        {
            "role": "system",
            "content": SYSTEM_PROMPT,
        },
        {
            "role": "user",
            "content": USER_PROMPT_FROM + TARGET_TEXT + USER_PROMPT_TO,
        },
    ],
    stream=True,
)

for chunk in response:
    # print(chunk)
    # print(chunk.choices[0].delta.content or "")
    # print("*" * 20)
    print(chunk.choices[0].delta.content or "", end="", flush=True)
