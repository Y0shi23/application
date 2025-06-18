#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ログインAPIのテストコード
"""

import requests
import json
import unittest
import time
from typing import Dict, Any

class LoginAPITest(unittest.TestCase):
    """ログインAPIのテストクラス"""
    
    def setUp(self):
        """テスト前の初期化"""
        self.base_url = "http://localhost:8080"  # Goアプリケーションのデフォルトポート
        self.login_url = f"{self.base_url}/auth/login"
        self.register_url = f"{self.base_url}/auth/register"
        self.profile_url = f"{self.base_url}/api/v1/profile"
        
        # テスト用ユーザー情報
        self.test_user = {
            "username": "testuser",
            "email": "test@example.com",
            "password": "testpassword123"
        }
        
        # レスポンスタイムアウト設定
        self.timeout = 10
        
    def test_login_success(self):
        """正常なログインテスト"""
        try:
            # まず、テストユーザーを登録
            register_response = requests.post(
                self.register_url,
                json=self.test_user,
                timeout=self.timeout
            )
            
            # ログインリクエスト
            login_data = {
                "username": self.test_user["username"],
                "password": self.test_user["password"]
            }
            
            response = requests.post(
                self.login_url,
                json=login_data,
                timeout=self.timeout
            )
            
            # ステータスコードの確認
            self.assertEqual(response.status_code, 200)
            
            # レスポンスボディの確認
            data = response.json()
            self.assertIn("message", data)
            self.assertIn("token", data)
            self.assertIn("user", data)
            self.assertEqual(data["message"], "Login successful")
            self.assertIsInstance(data["token"], str)
            self.assertTrue(len(data["token"]) > 0)
            self.assertEqual(data["user"]["username"], self.test_user["username"])
            
            print(f"✓ ログイン成功: {data['message']}")
            
        except requests.exceptions.RequestException as e:
            self.fail(f"リクエストエラー: {e}")
    
    def test_login_invalid_credentials(self):
        """無効な認証情報でのログインテスト"""
        login_data = {
            "username": "nonexistent_user",
            "password": "wrong_password"
        }
        
        try:
            response = requests.post(
                self.login_url,
                json=login_data,
                timeout=self.timeout
            )
            
            # ステータスコードの確認
            self.assertEqual(response.status_code, 401)
            
            # エラーメッセージの確認
            data = response.json()
            self.assertIn("error", data)
            self.assertEqual(data["error"], "Invalid credentials")
            
            print(f"✓ 無効な認証情報のエラー: {data['error']}")
            
        except requests.exceptions.RequestException as e:
            self.fail(f"リクエストエラー: {e}")
    
    def test_login_empty_username(self):
        """空のユーザー名でのログインテスト"""
        login_data = {
            "username": "",
            "password": "testpassword"
        }
        
        try:
            response = requests.post(
                self.login_url,
                json=login_data,
                timeout=self.timeout
            )
            
            # ステータスコードの確認
            self.assertEqual(response.status_code, 400)
            
            print("✓ 空のユーザー名エラー")
            
        except requests.exceptions.RequestException as e:
            self.fail(f"リクエストエラー: {e}")
    
    def test_login_empty_password(self):
        """空のパスワードでのログインテスト"""
        login_data = {
            "username": "testuser",
            "password": ""
        }
        
        try:
            response = requests.post(
                self.login_url,
                json=login_data,
                timeout=self.timeout
            )
            
            # ステータスコードの確認
            self.assertEqual(response.status_code, 400)
            
            print("✓ 空のパスワードエラー")
            
        except requests.exceptions.RequestException as e:
            self.fail(f"リクエストエラー: {e}")
    
    def test_login_missing_fields(self):
        """必須フィールドが不足しているリクエストのテスト"""
        # ユーザー名のみ
        login_data = {"username": "testuser"}
        
        try:
            response = requests.post(
                self.login_url,
                json=login_data,
                timeout=self.timeout
            )
            
            self.assertEqual(response.status_code, 400)
            print("✓ 必須フィールド不足エラー")
            
        except requests.exceptions.RequestException as e:
            self.fail(f"リクエストエラー: {e}")
    
    def test_login_invalid_json(self):
        """無効なJSONでのリクエストテスト"""
        invalid_json = '{"username": "testuser", "password":}'
        
        try:
            response = requests.post(
                self.login_url,
                data=invalid_json,
                headers={"Content-Type": "application/json"},
                timeout=self.timeout
            )
            
            self.assertEqual(response.status_code, 400)
            print("✓ 無効なJSONエラー")
            
        except requests.exceptions.RequestException as e:
            self.fail(f"リクエストエラー: {e}")
    
    def test_login_sql_injection_attempt(self):
        """SQLインジェクション攻撃の試行テスト"""
        malicious_data = {
            "username": "admin'; DROP TABLE users; --",
            "password": "password"
        }
        
        try:
            response = requests.post(
                self.login_url,
                json=malicious_data,
                timeout=self.timeout
            )
            
            # SQLインジェクションが防がれて、通常の認証エラーが返されることを確認
            self.assertEqual(response.status_code, 401)
            
            data = response.json()
            self.assertEqual(data["error"], "Invalid credentials")
            
            print("✓ SQLインジェクション対策OK")
            
        except requests.exceptions.RequestException as e:
            self.fail(f"リクエストエラー: {e}")
    
    def test_login_and_access_protected_resource(self):
        """ログイン後に保護されたリソースにアクセスするテスト"""
        try:
            # ユーザー登録
            register_response = requests.post(
                self.register_url,
                json=self.test_user,
                timeout=self.timeout
            )
            
            # ログイン
            login_data = {
                "username": self.test_user["username"],
                "password": self.test_user["password"]
            }
            
            login_response = requests.post(
                self.login_url,
                json=login_data,
                timeout=self.timeout
            )
            
            self.assertEqual(login_response.status_code, 200)
            
            # JWTトークンを取得
            token = login_response.json()["token"]
            
            # 保護されたリソースにアクセス
            headers = {"Authorization": f"Bearer {token}"}
            profile_response = requests.get(
                self.profile_url,
                headers=headers,
                timeout=self.timeout
            )
            
            self.assertEqual(profile_response.status_code, 200)
            
            profile_data = profile_response.json()
            self.assertIn("user", profile_data)
            self.assertEqual(profile_data["user"]["username"], self.test_user["username"])
            
            print("✓ ログイン後の保護されたリソースアクセス成功")
            
        except requests.exceptions.RequestException as e:
            self.fail(f"リクエストエラー: {e}")
    
    def test_login_performance(self):
        """ログインAPIのパフォーマンステスト"""
        login_data = {
            "username": self.test_user["username"],
            "password": self.test_user["password"]
        }
        
        # 5回のリクエストでレスポンス時間を測定
        response_times = []
        
        for i in range(5):
            start_time = time.time()
            
            try:
                response = requests.post(
                    self.login_url,
                    json=login_data,
                    timeout=self.timeout
                )
                
                end_time = time.time()
                response_time = end_time - start_time
                response_times.append(response_time)
                
            except requests.exceptions.RequestException as e:
                self.fail(f"リクエストエラー: {e}")
        
        # 平均レスポンス時間を計算
        avg_response_time = sum(response_times) / len(response_times)
        
        # レスポンス時間が2秒以内であることを確認
        self.assertLess(avg_response_time, 2.0)
        
        print(f"✓ 平均レスポンス時間: {avg_response_time:.3f}秒")


class LoginUtilityTest(unittest.TestCase):
    """ログイン関連のユーティリティテスト"""
    
    def test_validate_login_data(self):
        """ログインデータの検証テスト"""
        
        def validate_login_data(data: Dict[str, Any]) -> bool:
            """ログインデータの検証"""
            if not isinstance(data, dict):
                return False
            
            required_fields = ["username", "password"]
            for field in required_fields:
                if field not in data or not data[field] or not isinstance(data[field], str):
                    return False
            
            # ユーザー名の長さチェック
            if len(data["username"]) < 3 or len(data["username"]) > 50:
                return False
            
            # パスワードの長さチェック
            if len(data["password"]) < 6:
                return False
            
            return True
        
        # 正常なデータ
        valid_data = {"username": "testuser", "password": "password123"}
        self.assertTrue(validate_login_data(valid_data))
        
        # 無効なデータ
        invalid_data_cases = [
            {},  # 空のデータ
            {"username": ""},  # 空のユーザー名
            {"password": "123"},  # パスワードが短い
            {"username": "ab", "password": "password"},  # ユーザー名が短い
            {"username": "a" * 51, "password": "password"},  # ユーザー名が長い
            None,  # None
            "invalid",  # 文字列
        ]
        
        for invalid_data in invalid_data_cases:
            self.assertFalse(validate_login_data(invalid_data))
        
        print("✓ ログインデータ検証テスト完了")


def run_all_tests():
    """すべてのテストを実行"""
    print("=" * 50)
    print("ログインAPIテスト開始")
    print("=" * 50)
    
    # テストスイートを作成
    test_suite = unittest.TestSuite()
    
    # LoginAPITestクラスのテストを追加
    test_suite.addTest(unittest.makeSuite(LoginAPITest))
    test_suite.addTest(unittest.makeSuite(LoginUtilityTest))
    
    # テストランナーを作成して実行
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(test_suite)
    
    print("=" * 50)
    print(f"テスト完了: {result.testsRun}件実行")
    print(f"成功: {result.testsRun - len(result.failures) - len(result.errors)}件")
    print(f"失敗: {len(result.failures)}件")
    print(f"エラー: {len(result.errors)}件")
    print("=" * 50)
    
    return result.wasSuccessful()


if __name__ == "__main__":
    # APIサーバーが起動していることを確認
    try:
        response = requests.get("http://localhost:8080/health", timeout=5)
        if response.status_code == 200:
            print("✓ APIサーバーが起動していることを確認")
            run_all_tests()
        else:
            print("❌ APIサーバーが正常に応答しません")
    except requests.exceptions.RequestException:
        print("❌ APIサーバーに接続できません。")
        print("バックエンドサーバーが起動していることを確認してください。")
        print("起動コマンド: cd backend/app && go run main.go") 