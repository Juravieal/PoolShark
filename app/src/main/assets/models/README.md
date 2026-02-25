# Model Assets

Place the TFLite billiard ball detection model here:

**File:** `billiard_detector.tflite`

## Obtaining the Model

1. Visit https://roboflow.com/ → search "billiard balls" or "pool balls"
2. Find a model with classes: `cue_ball`, `solid`, `stripe`, `eight_ball`
3. Export → TFLite (float32) → download
4. Rename to `billiard_detector.tflite` and place in this directory

## Model Spec

- Input: `[1, 640, 640, 3]` float32 NHWC, normalized 0..1
- Output: `[1, 5+numClasses, 8400]` — cx, cy, w, h, class scores per anchor
- Classes (index order): 0=CUE_BALL, 1=SOLID, 2=STRIPE, 3=EIGHT_BALL
- Expected size: ~5 MB (YOLOv8n)
