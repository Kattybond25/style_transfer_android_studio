import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
from tensorflow.keras.preprocessing import image as kp_image
import os
import tarfile

def load_img(path_to_img):
    """Load and preprocess the image from a given path."""
    max_dim = 512  # Adjust size based on your memory constraints
    img = kp_image.load_img(path_to_img)
    img = kp_image.img_to_array(img)

    # Resize to fit the model requirements
    img = tf.image.resize_with_pad(img, max_dim, max_dim)

    # Add batch dimension
    img = img[tf.newaxis, :]
    img = tf.keras.applications.vgg19.preprocess_input(img)
    return img

def display_img(img, title=None):
    """Display an image tensor."""
    out_img = tf.squeeze(img, axis=0)
    out_img = tf.clip_by_value(out_img, 0, 255).numpy().astype('uint8')

    plt.imshow(out_img)
    plt.axis('off')
    if title:
        plt.title(title)
    plt.show()


# Define paths to the content and style images
example_content_path = '/home/alex/AndroidStudioProjects/MyPrismaApp/app/src/main/res/drawable/gus.jpg'  # Replace with an actual content image in your dataset
example_style_path = '/home/alex/AndroidStudioProjects/MyPrismaApp/app/src/main/res/drawable/first.jpg'

# Load and display content and style images
content_image = load_img(example_content_path)
style_image = load_img(example_style_path)

# Display images to verify
display_img(content_image, title="Content Image")
display_img(style_image, title="Style Image")
