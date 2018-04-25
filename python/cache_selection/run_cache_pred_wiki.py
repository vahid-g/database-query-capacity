''' runs simple classification on cache selection problem '''
from __future__ import division
import sys
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
# from sklearn.model_selection import GridSearchCV
# from sklearn.model_selection import StratifiedShuffleSplit
from sklearn.preprocessing import StandardScaler
from sklearn import linear_model
# from sklearn.metrics import precision_score
# from sklearn import svm
from sklearn.ensemble import RandomForestClassifier
from cache_pred import train_lr

def main(argv):
    filename = argv[0]
    df = pd.read_csv('../../data/python_data/' + filename)
    df = train_wiki(df)
    if (argv[1]):
        df.to_csv('%s%s_result.csv' % ('../../data/python_data/',
                                       filename[:-4]), index=False)

if __name__ == "__main__":
    main(sys.argv[1:])
