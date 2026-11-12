
import ollama
import json
from pathlib import Path

MODEL = "gemma3:4b"


def analyze_road(image_path, street_type):
    """
    Receives one road photograph and returns
    YES / NO / UNKNOWN for each accessibility tag.
    """

    image_path = Path(image_path)

    if not image_path.exists():
        raise FileNotFoundError(f"Image not found: {image_path}")

    prompt = """
    You are Jarvis, an AI road accessibility image analyzer.

    Analyze the provided road photograph.

    Determine the following four features:

    1. hasSide: Is a sidewalk visibly present?
    2. hasBike: Is a bicycle lane visibly present?
    3. hasTree: Are trees visibly present?
    4. hasGreen: Is green space, such as grass or landscaping,
       visibly present?

    Return ONLY valid JSON using exactly this structure:

    {
        "hasSide": "UNKNOWN",
        "hasBike": "UNKNOWN",
        "hasTree": "UNKNOWN",
        "hasGreen": "UNKNOWN"
    }

    Rules:
    - Use only YES, NO, or UNKNOWN.
    - YES means the feature is visibly present.
    - NO means the image provides enough evidence that the
      feature is absent.
    - UNKNOWN means the image is unclear, obstructed, or
      does not show enough of the area to determine the answer.
    - Do not assume something is absent because it is outside
      the photograph.
    - Do not guess.
    - Do not include explanations or additional fields.
    """

    response = ollama.chat(
        model=MODEL,
        format="json",
        messages=[
            {
                "role": "user",
                "content": prompt,
                "images": [str(image_path.resolve())]
            }
        ]
    )

    # Convert the AI response into a Python dictionary
    findings = json.loads(response.message.content)

    # Validate the expected fields and values
    expected_fields = [
        "hasSide",
        "hasBike",
        "hasTree",
        "hasGreen"
    ]

    for field in expected_fields:
        if field not in findings:
            raise ValueError(f"Missing JSON field: {field}")

        if findings[field] not in ["YES", "NO", "UNKNOWN"]:
            raise ValueError(f"Invalid value for {field}")

    return {
    "hasSide": findings["hasSide"],
    "hasBike": findings["hasBike"],
    "hasTree": findings["hasTree"],
    "hasGreen": findings["hasGreen"],
    "streetType": street_type
}


if __name__ == "__main__":

    image_path = "images/road1.jpg"
    street_type = "STREET"

    try:
        results = analyze_road(image_path, street_type)

        # Create a separate JSON file for this image
        output_path = Path(image_path).with_suffix(".json")

        with open(output_path, "w") as file:
            json.dump(results, file, indent=4)

        print("Jarvis analysis complete!")
        print(json.dumps(results, indent=4))
        print(f"Saved to {output_path}")

    except Exception as error:
        print(f"Analysis failed: {error}")
