from flask import Flask, request, render_template, jsonify
from flask_cors import CORS
from ch import getStockData

app = Flask(__name__)

CORS(app)

@app.route("/api/getStockData", methods=["POST"])
def gStockData():
    data = request.get_json()
    return getStockData(data['symbol'])

    


if __name__ == "__main__":
    app.run(debug=True, port=5000)